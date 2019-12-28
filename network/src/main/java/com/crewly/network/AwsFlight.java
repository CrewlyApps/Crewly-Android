package com.crewly.network;

import androidx.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.HashSet;
import java.util.Iterator;
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
    @NonNull
    public String getId() { if (id != null) { return id; } else { return ""; } }
    public void setId(String id) { this.id = id; }

    @DynamoDBRangeKey(attributeName = AwsModelKeys.Flight.COMPANY_ID)
    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.COMPANY_ID)
    @NonNull
    public Integer getCompanyId() { if (companyId != null) { return companyId; } else { return -1; } }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.AIRPORT_ORIGIN)
    @NonNull
    public String getAirportOrigin() { if (airportOrigin != null) { return airportOrigin; } else { return ""; } }
    public void setAirportOrigin(String airportOrigin) { this.airportOrigin = airportOrigin; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.COUNTRY_ORIGIN)
    @NonNull
    public String getCountryOrigin() { if (countryOrigin != null) { return countryOrigin; } else { return ""; } }
    public void setCountryOrigin(String countryOrigin) { this.countryOrigin = countryOrigin; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.DATE)
    @NonNull
    public String getDate() { if (date != null) { return date; } else { return ""; } }
    public void setDate(String date) { this.date = date; }

    @DynamoDBAttribute(attributeName = AwsModelKeys.Flight.CREW)
    @NonNull
    public Set<String> getCrewIds() { if (crewIds != null) { return crewIds; } else { return new HashSet<>(); } }
    public void setCrewIds(Set<String> crewIds) { this.crewIds = crewIds; }

    @NonNull
    @Override
    public String toString() {
        String crewIds = "";
        Iterator<String> iterator = this.crewIds.iterator();
        while (iterator.hasNext()) {
            crewIds += iterator.next();
        }

        return "id=" + id + ", companyId=" + companyId + ", airportOrigin=" + airportOrigin +
                ", countryOrigin=" + countryOrigin + ", date=" + date + ", crewIds=" + crewIds;
    }
}
