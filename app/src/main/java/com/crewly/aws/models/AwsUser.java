package com.crewly.aws.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBNativeBoolean;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Derek on 04/05/2019
 */
@DynamoDBTable(tableName = "crewly-mobilehub-609062562-User")
public class AwsUser {

    private String id;
    private Integer companyId;
    private String base;
    private Boolean isPilot;
    private Boolean isPremium;
    private Boolean isVisible;
    private String joinedDate;
    private String lastSeenDate;
    private String name;
    private Integer rankId;
    private String registrationDate;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAttribute(attributeName = "id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDBRangeKey(attributeName = "companyId")
    @DynamoDBAttribute(attributeName = "companyId")
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    @DynamoDBAttribute(attributeName = "base")
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = "isPilot")
    public Boolean getIsPilot() { return isPilot; }
    public void setIsPilot(Boolean isPilot) { this.isPilot = isPilot; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = "isPremium")
    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = "isVisible")
    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    @DynamoDBAttribute(attributeName = "joinedDate")
    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }

    @DynamoDBAttribute(attributeName = "lastSeenDate")
    public String getLastSeenDate() { return lastSeenDate; }
    public void setLastSeenDate(String lastSeenDate) { this.lastSeenDate = lastSeenDate; }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDBAttribute(attributeName = "rankId")
    public Integer getRankId() { return rankId; }
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    @DynamoDBAttribute(attributeName = "registrationDate")
    public String getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}
