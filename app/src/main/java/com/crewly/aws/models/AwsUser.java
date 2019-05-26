package com.crewly.aws.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBNativeBoolean;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.crewly.aws.AwsTableNames;
import com.crewly.crew.Rank;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    @NonNull
    public String getId() { if (id != null) { return id; } else { return ""; } }
    public void setId(String id) { this.id = id; }

    @DynamoDBRangeKey(attributeName = AwsModelKeys.User.COMPANY_ID)
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.COMPANY_ID)
    @NonNull
    public Integer getCompanyId() { if (companyId != null) { return companyId; } else { return -1; } }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.BASE)
    @NonNull
    public String getBase() { if (base != null) { return base; } else { return ""; } }
    public void setBase(String base) { this.base = base; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.IS_PILOT)
    @NonNull
    public Boolean getIsPilot() { if (isPilot != null) { return isPilot; } else { return false; } }
    public void setIsPilot(Boolean isPilot) { this.isPilot = isPilot; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.IS_PREMIUM)
    @Nullable
    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }

    @DynamoDBNativeBoolean
    @DynamoDBAttribute(attributeName = AwsModelKeys.User.IS_VISIBLE)
    @NonNull
    public Boolean getIsVisible() { if (isVisible != null) { return isVisible; } else { return false; } }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.JOINED_DATE)
    @NonNull
    public String getJoinedDate() { if (joinedDate != null) { return joinedDate; } else { return ""; } }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.LAST_SEEN_DATE)
    @NonNull
    public String getLastSeenDate() { if (lastSeenDate != null) { return lastSeenDate; } else { return ""; } }
    public void setLastSeenDate(String lastSeenDate) { this.lastSeenDate = lastSeenDate; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.NAME)
    @NonNull
    public String getName() { if (name != null) { return name; } else { return ""; } }
    public void setName(String name) { this.name = name; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.RANK_ID)
    @NonNull
    public Integer getRankId() { if (rankId != null) { return rankId; } else { return Rank.NONE.getValue(); } }
    public void setRankId(Integer rankId) { this.rankId = rankId; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.User.REGISTRATION_DATE)
    @Nullable
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}
