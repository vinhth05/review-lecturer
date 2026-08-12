package com.example.ctu.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.ctu.entity.enums.ReportResolution;
import com.example.ctu.entity.enums.ReportStatus;
import com.example.ctu.exception.ConflictException;

class ReportWorkflowTest {

    private final User moderator = User.builder().id(42L).build();

    @Test
    void pendingReport_canBeDismissedWithAuditMetadata() {
        Report report = Report.builder().status(ReportStatus.PENDING).build();

        report.resolve(ReportResolution.DISMISS, "No policy violation was found", moderator);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.DISMISSED);
        assertThat(report.getResolutionNote()).isEqualTo("No policy violation was found");
        assertThat(report.getResolvedBy()).isSameAs(moderator);
        assertThat(report.getResolvedAt()).isNotNull();
    }

    @Test
    void reportCannotBeResolvedTwice() {
        Report report = Report.builder().status(ReportStatus.PENDING).build();
        report.resolve(ReportResolution.REJECT_REVIEW, "Confirmed policy violation", moderator);

        assertThatThrownBy(() -> report.resolve(ReportResolution.DISMISS, "Changed mind", moderator))
                .isInstanceOf(ConflictException.class);
    }
}
