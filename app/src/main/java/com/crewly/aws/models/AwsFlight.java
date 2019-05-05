package com.crewly.aws.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.Set;

/**
 * Created by Derek on 04/05/2019
 */
@DynamoDBTable(tableName = "crewly-mobilehub-609062562-Flight")
public class AwsFlight {

    private String id;
    private Integer companyId;
    private String airportOrigin;
    private String countryOrigin;
    private String date;
    private Set<String> crewIds;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAttribute(attributeName = "id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDBRangeKey(attributeName = "companyId")
    @DynamoDBAttribute(attributeName = "companyId")
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    @DynamoDBAttribute(attributeName = "airportOrigin")
    public String getAirportOrigin() { return airportOrigin; }
    public void setAirportOrigin(String airportOrigin) { this.airportOrigin = airportOrigin; }

    @DynamoDBAttribute(attributeName = "countryOrigin")
    public String getCountryOrigin() { return countryOrigin; }
    public void setCountryOrigin(String countryOrigin) { this.countryOrigin = countryOrigin; }

    @DynamoDBAttribute(attributeName = "date")
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @DynamoDBAttribute(attributeName = "crew")
    public Set<String> getCrewIds() { return crewIds; }
    public void setCrewIds(Set<String> crewIds) { this.crewIds = crewIds; }
}
