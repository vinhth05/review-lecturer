package com.example.ctu.service;

import com.example.ctu.dto.admin.AdminDtos;
import com.example.ctu.entity.Faculty;
import com.example.ctu.entity.Lecturer;
import com.example.ctu.entity.enums.LecturerStatus;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.repository.FacultyRepository;
import com.example.ctu.repository.LecturerRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class CtuLecturerImportService {

    private static final String CTU_STAFF_BASE = "https://www.ctu.edu.vn/webctu_staff/staff.php";

    private final LecturerRepository lecturerRepository;
    private final FacultyRepository facultyRepository;

    public CtuLecturerImportService(LecturerRepository lecturerRepository, FacultyRepository facultyRepository) {
        this.lecturerRepository = lecturerRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional
    public AdminDtos.ImportCtuLecturersResponse importFromCtu(AdminDtos.ImportCtuLecturersRequest request) {
        int maxPages = request.maxPages() == null ? 3 : Math.max(1, Math.min(30, request.maxPages()));
        Faculty fallbackFaculty = resolveFallbackFaculty(request.fallbackFacultyId());

        int fetchedRows = 0;
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (int page = 1; page <= maxPages; page++) {
            String url = page == 1 ? CTU_STAFF_BASE : CTU_STAFF_BASE + "?page=" + page;
            try {
                Document document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (compatible; CTUReviewImporter/1.0)")
                        .timeout(15000)
                        .get();

                Elements rows = document.select("table tr");
                if (rows.isEmpty()) {
                    errors.add("Không tìm thấy bảng dữ liệu ở trang: " + url);
                    continue;
                }

                for (Element row : rows) {
                    Elements cells = row.select("td");
                    if (cells.size() < 5) {
                        continue;
                    }

                    String name = clean(cells.get(0).text());
                    String specialization = clean(cells.get(4).text());
                    if (name.isBlank()) {
                        continue;
                    }

                    fetchedRows++;
                    String lecturerCode = generateLecturerCode(name);
                    if (lecturerRepository.existsByFullNameIgnoreCase(name) || lecturerRepository.existsByLecturerCode(lecturerCode)) {
                        skipped++;
                        continue;
                    }

                    Faculty faculty = mapFacultyBySpecialization(specialization).orElse(fallbackFaculty);
                    Lecturer lecturer = Lecturer.builder()
                            .lecturerCode(lecturerCode)
                            .fullName(name)
                            .faculty(faculty)
                            .subject(null)
                            .status(LecturerStatus.ACTIVE)
                            .build();
                    lecturerRepository.save(lecturer);
                    imported++;
                }
            } catch (IOException exception) {
                errors.add("Lỗi tải trang " + url + ": " + exception.getMessage());
            }
        }

        return new AdminDtos.ImportCtuLecturersResponse(maxPages, fetchedRows, imported, skipped, errors);
    }

    private Faculty resolveFallbackFaculty(UUID fallbackFacultyId) {
        if (fallbackFacultyId != null) {
            return facultyRepository.findById(fallbackFacultyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Fallback faculty không tồn tại"));
        }
        return facultyRepository.findAll().stream()
                .sorted(Comparator.comparing(Faculty::getName))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Cần có ít nhất 1 khoa trước khi import giảng viên"));
    }

    private Optional<Faculty> mapFacultyBySpecialization(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return Optional.empty();
        }
        String normalized = specialization.toLowerCase(Locale.ROOT);
        return facultyRepository.findAll().stream()
                .filter(faculty -> normalized.contains(tokenizeFacultyName(faculty.getName())))
                .findFirst();
    }

    private String tokenizeFacultyName(String facultyName) {
        String normalized = facultyName.toLowerCase(Locale.ROOT)
                .replace("khoa", "")
                .replace("công nghệ", "")
                .trim();
        if (normalized.contains("thông tin")) {
            return "tin";
        }
        if (normalized.contains("kinh tế")) {
            return "kinh tế";
        }
        if (normalized.contains("sư phạm")) {
            return "giáo dục";
        }
        if (normalized.contains("thực phẩm")) {
            return "thực phẩm";
        }
        if (normalized.contains("nông nghiệp")) {
            return "nông";
        }
        if (normalized.contains("thủy sản")) {
            return "thủy sản";
        }
        return normalized;
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String generateLecturerCode(String name) {
        String base = name.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "")
                .replaceAll("\u0110", "D");
        if (base.length() > 8) {
            base = base.substring(0, 8);
        }
        String candidate = "CTU" + base;
        int index = 1;
        while (lecturerRepository.existsByLecturerCode(candidate)) {
            candidate = "CTU" + base + String.format(Locale.ROOT, "%02d", index++);
        }
        return candidate;
    }
}
