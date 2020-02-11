[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3ba6416fd11d41fdaf281e7dab6042dc)](https://www.codacy.com/app/philwhiles/census-mock-case-api-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/census-mock-case-api-service&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.com/ONSdigital/census-mock-case-api-service.svg?branch=master)](https://travis-ci.com/ONSdigital/census-mock-case-api-service)
[![codecov](https://codecov.io/gh/ONSdigital/census-mock-case-api-service/branch/master/graph/badge.svg)](https://codecov.io/gh/ONSdigital/census-mock-case-api-service)


# Census Mock Case API Service
This repository is a test service and can be run instead of the case api service to test other services. It returns cases and questionnaires that are stored as JSON in 2 yml files 
(cases.yml and questionnaires.yml) in the resources folder of the project. The project facilitates testing of services that rely on the real case api services, currently the contact centre service and filed services. 

## Set Up
Do the following steps to set up the code to run locally:
* Install Java 11 locally
* Make sure that you have a suitable settings.xml file in your local .m2 directory
* Clone the census-contact-centre locally

## Running

There are two ways of running this service

* The first way is from the command line after moving into the same directory as the pom.xml:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
* The second way requires that you first create a JAR file using the following mvn command (after moving into the same directory as the pom.xml):
    ```bash
    mvn clean package
    ```
This will create the JAR file in the Target directory. You can then right-click on the JAR file (in Intellij) and choose 'Run'.

## End Point

When running successfully version information can be obtained from the info endpoint
    
* localhost:8161/cases/info

Data examples can be viewed from the examples endpoint

* localhost:8161/cases/examples

## Manual testing

### Adding case data

To add a new case:

    // Create case data file
    cat > /tmp/new_case.json << EOF
    [
      {
       "caseRef": "6757766",
       "arid": "2344266233",
       "estabArid": "AABBCC",
       "estabType": "ET",
       "uprn": "1347459999",
       "createdDateTime": "2020-01-09T11:52:05.006+01:00",
       "addressLine1": "Napier House",
       "addressLine2": "88 Harbour Street",
       "addressLine3": "Parkhead",
       "townName": "Glasgow",
       "postcode": "G1 2AA",
       "organisationName": "ON",
       "addressLevel": "E",
       "abpCode": "AACC",
       "latitude": "41.40338",
       "longitude": "2.17403",
       "oa": "EE22",
       "lsoa": "x1",
       "msoa": "x2",
       "lad": "H1",
       "caseEvents": [],
       "id": "0779ccfb-584c-486f-b36f-667fdf7f8723",
       "caseType": "CE",
       "region": "E",
       "state": "ACTIONABLE",
       "collectionExerciseId": "6334804d-fc8e-4838-b4ee-9a95ea969488",
       "surveyType": "CCS"
     }
     ]
    EOF
    
    // Publish the new case(s) to the fake case service
    curl -s --data @/tmp/new_case.json -H "Content-Type: application/json" http://localhost:8161/cases/data/cases/add


### New questionnaire ID

To get a new Questionnaire ID for a case:
    
    // Get new CE questionnaire ID for a CE
    curl -s -H "Content-Type: application/json" "http://localhost:8161/cases/0779ccfb-584c-486f-b36f-667fdf7f8723/qid" | jq
    // Get new questionnaire ID for individual in a CE
    curl -s -H "Content-Type: application/json" "http://localhost:8161/cases/0779ccfb-584c-486f-b36f-667fdf7f8723/qid?individual=true" | jq

    // Get new HH questionnaire ID for a HH
    curl -s -H "Content-Type: application/json" "http://localhost:8161/cases/3305e937-6fb1-4ce1-9d4c-077f147789ac/qid" | jq
    // Get new questionnaireId for an individual in a HH
    curl -s -H "Content-Type: application/json" "http://localhost:8161/cases/3305e937-6fb1-4ce1-9d4c-077f147789ac/qid?individual=true&individualCaseId=f91995ac-71d8-42d5-a454-edd02028de73" | jq


## Docker image build
Is switched off by default for clean deploy. Switch on with;

* mvn dockerfile:build -Dskip.dockerfile=false

    
## Copyright
Copyright (C) 2019 Crown Copyright (Office for National Statistics)

