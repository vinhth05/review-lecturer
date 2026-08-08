package com.example.ctu.dto.metadata;

public final class MetadataDtos {
    private MetadataDtos() {}

    public record FacultyResponse(
            Long id,
            String name,
            String code
    ) {}

    public record SubjectResponse(
            Long id,
            String name,
            String code,
            Long facultyId,
            String facultyCode,
            String facultyName
    ) {}
}
