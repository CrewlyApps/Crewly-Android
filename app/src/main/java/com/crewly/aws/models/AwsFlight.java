package com.crewly.aws.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.crewly.aws.AwsTableNames;

import java.util.Set;

/**
 * Created by Derek on 04/05/2019
 */
@DynamoDBTable(tableName = AwsTableNames.FLIGHT)
public class AwsFlight {

    private String id;
    private Integer companyId;
    private String airportOrigin;
    private String countryOrigin;
    private String date;
    private Set<String> crewIds;

    @DynamoDBHashKey(attributeName = AwsModelKeys.Flight.ID)
    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.ID)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDBRangeKey(attributeName = AwsModelKeys.Flight.COMPANY_ID)
    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.COMPANY_ID)
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.AIRPORT_ORIGIN)
    public String getAirportOrigin() { return airportOrigin; }
    public void setAirportOrigin(String airportOrigin) { this.airportOrigin = airportOrigin; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.COUNTRY_ORIGIN)
    public String getCountryOrigin() { return countryOrigin; }
    public void setCountryOrigin(String countryOrigin) { this.countryOrigin = countryOrigin; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.DATE)
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.CREW)
    public Set<String> getCrewIds() { return crewIds; }
    public void setCrewIds(Set<String> crewIds) { this.crewIds = crewIds; }
}
