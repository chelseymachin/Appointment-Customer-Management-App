package model;

public class FirstLevelDivision {
    public int firstLevelDivisionId;
    public int countryId;
    public String firstLevelDivisionName;

    public FirstLevelDivision(int firstLevelDivisionId, int countryId, String firstLevelDivisionName) {
        this.firstLevelDivisionId = firstLevelDivisionId;
        this.countryId = countryId;
        this.firstLevelDivisionName = firstLevelDivisionName;
    }

    public FirstLevelDivision(int firstLevelDivisionId, String firstLevelDivisionName) {
        this.firstLevelDivisionName = firstLevelDivisionName;
        this.firstLevelDivisionId = firstLevelDivisionId;
    }

    public int getFirstLevelDivisionId() {
        return firstLevelDivisionId;
    }

    public void setFirstLevelDivisionId(int firstLevelDivisionId) {
        this.firstLevelDivisionId = firstLevelDivisionId;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getFirstLevelDivisionName() {
        return firstLevelDivisionName;
    }

    public void setFirstLevelDivisionName(String firstLevelDivisionName) {
        this.firstLevelDivisionName = firstLevelDivisionName;
    }

    @Override public String toString() {
        return (firstLevelDivisionName);
    }
}
