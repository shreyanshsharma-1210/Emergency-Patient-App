package com.emergency.patient.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PatientProfile implements Serializable {
    
    // Demographic Data (Step 1)
    private String fullName;
    private long dobMillis;
    private String gender;
    private String bloodGroup;
    private String profilePhotoUri;

    // Triage Quiz Data (Step 2)
    private boolean hasSevereAllergies;
    private String severeAllergiesDetails;
    
    private boolean hasCardioIssues;
    private String cardioIssuesDetails;
    
    private boolean hasChronicConditions;
    private String chronicConditionsDetails;
    
    private boolean hasImplants;
    private String implantsDetails;
    
    private boolean hasCriticalMeds;
    private String criticalMedsDetails;

    // Emergency Contacts (Step 1b)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    public static class EmergencyContact implements Serializable {
        public String name;
        public String phoneNumber;

        public EmergencyContact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }
    }

    public PatientProfile() {
    }

    // Getters and Setters for Emergency Contacts
    public List<EmergencyContact> getEmergencyContacts() { return emergencyContacts; }
    public void setEmergencyContacts(List<EmergencyContact> contacts) { this.emergencyContacts = contacts; }
    public void addEmergencyContact(String name, String phone) {
        if (emergencyContacts.size() < 3) {
            emergencyContacts.add(new EmergencyContact(name, phone));
        }
    }

    // Getters and Setters for Demographics
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public long getDobMillis() { return dobMillis; }
    public void setDobMillis(long dobMillis) { this.dobMillis = dobMillis; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getProfilePhotoUri() { return profilePhotoUri; }
    public void setProfilePhotoUri(String profilePhotoUri) { this.profilePhotoUri = profilePhotoUri; }

    // Getters and Setters for Triage
    public boolean isHasSevereAllergies() { return hasSevereAllergies; }
    public void setHasSevereAllergies(boolean hasSevereAllergies) { this.hasSevereAllergies = hasSevereAllergies; }

    public String getSevereAllergiesDetails() { return severeAllergiesDetails; }
    public void setSevereAllergiesDetails(String severeAllergiesDetails) { this.severeAllergiesDetails = severeAllergiesDetails; }

    public boolean isHasCardioIssues() { return hasCardioIssues; }
    public void setHasCardioIssues(boolean hasCardioIssues) { this.hasCardioIssues = hasCardioIssues; }

    public String getCardioIssuesDetails() { return cardioIssuesDetails; }
    public void setCardioIssuesDetails(String cardioIssuesDetails) { this.cardioIssuesDetails = cardioIssuesDetails; }

    public boolean isHasChronicConditions() { return hasChronicConditions; }
    public void setHasChronicConditions(boolean hasChronicConditions) { this.hasChronicConditions = hasChronicConditions; }

    public String getChronicConditionsDetails() { return chronicConditionsDetails; }
    public void setChronicConditionsDetails(String chronicConditionsDetails) { this.chronicConditionsDetails = chronicConditionsDetails; }

    public boolean isHasImplants() { return hasImplants; }
    public void setHasImplants(boolean hasImplants) { this.hasImplants = hasImplants; }

    public String getImplantsDetails() { return implantsDetails; }
    public void setImplantsDetails(String implantsDetails) { this.implantsDetails = implantsDetails; }

    public boolean isHasCriticalMeds() { return hasCriticalMeds; }
    public void setHasCriticalMeds(boolean hasCriticalMeds) { this.hasCriticalMeds = hasCriticalMeds; }

    public String getCriticalMedsDetails() { return criticalMedsDetails; }
    public void setCriticalMedsDetails(String criticalMedsDetails) { this.criticalMedsDetails = criticalMedsDetails; }

    // Utility to get a flattened list of active conditions for display
    public List<String> getActiveConditionsList() {
        List<String> conditions = new ArrayList<>();
        if (hasSevereAllergies && severeAllergiesDetails != null && !severeAllergiesDetails.isEmpty()) {
            conditions.add("Allergies: " + severeAllergiesDetails);
        }
        if (hasCardioIssues && cardioIssuesDetails != null && !cardioIssuesDetails.isEmpty()) {
            conditions.add("Cardio: " + cardioIssuesDetails);
        }
        if (hasChronicConditions && chronicConditionsDetails != null && !chronicConditionsDetails.isEmpty()) {
            conditions.add("Chronic: " + chronicConditionsDetails);
        }
        if (hasImplants && implantsDetails != null && !implantsDetails.isEmpty()) {
            conditions.add("Implants: " + implantsDetails);
        }
        if (hasCriticalMeds && criticalMedsDetails != null && !criticalMedsDetails.isEmpty()) {
            conditions.add("Meds: " + criticalMedsDetails);
        }
        return conditions;
    }
}
