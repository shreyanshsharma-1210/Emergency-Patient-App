package com.emergency.patient.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(
    tableName = "health_documents",
    indices = {@Index("patientUuid")}
)
public class HealthDocumentEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String patientUuid;
    public String displayName;
    public String internalFilePath;
    public String extractionStatus; // e.g., "pending", "success", "failed"
    public String extractedJson;
    public long uploadTimestamp;

    public HealthDocumentEntity(String patientUuid, String displayName, String internalFilePath) {
        this.patientUuid = patientUuid;
        this.displayName = displayName;
        this.internalFilePath = internalFilePath;
        this.uploadTimestamp = System.currentTimeMillis();
        this.extractionStatus = "pending";
    }
}
