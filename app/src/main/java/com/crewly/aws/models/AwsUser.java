package com.crewly.aws.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBNativeBoolean;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.crewly.aws.AwsTableNames;

/**
 * Created by Derek on 04/05/2019
 */
@DynamoDBTable(tableName = AwsTableNames.USER)
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

    @DynamoDBHashKey(attributeName = AwsModelKeys.User.ID)
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.ID)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDBRangeKey(attributeName = AwsModelKeys.User.COMPANY_ID)
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.COMPANY_ID)
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.BASE)
    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.IS_PILOT)
    public Boolean getIsPilot() { return isPilot; }
    public void setIsPilot(Boolean isPilot) { this.isPilot = isPilot; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.IS_PREMIUM)
    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.IS_VISIBLE)
    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.JOINED_DATE)
    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.LAST_SEEN_DATE)
    public String getLastSeenDate() { return lastSeenDate; }
    public void setLastSeenDate(String lastSeenDate) { this.lastSeenDate = lastSeenDate; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.NAME)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.RANK_ID)
    public Integer getRankId() { return rankId; }
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.REGISTRATION_DATE)
    public String getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}
