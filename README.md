# TRMS: Form Service

## Project Description
The Form Service API for the Tuition Reimbursement Management System manages form data for the application. Users submit reimbursement request forms for business-relevant events they
would like to attend, what it costs and how it relates to company business and then a hierarchy of approvers either grant or deny the request.

## Technologies Used
![](https://img.shields.io/badge/-Java-007396?style=flat-square&logo=java&logoColor=white)
![](https://img.shields.io/badge/-Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![](https://img.shields.io/badge/-Spring_Webflux-236DB33F?style=flat-square&logo=spring&logoColor=white)
![](https://img.shields.io/badge/-Cassandra-1287B1?style=flat-square&logo=apachecassandra&logoColor=white)
![](https://img.shields.io/badge/-RabbitMQ-23FF66?style=flat-square&logo=rabbitmq&color=white)
![Spring Security](https://img.shields.io/badge/-Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white)
![JUnit](https://img.shields.io/badge/-JUnit-25A162?style=flat-square&logo=junit5&logoColor=white)
![Docker](https://img.shields.io/badge/-Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/-AWS-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![Maven](https://img.shields.io/badge/-Maven-C71A36?style=flat-square&logo=apache-maven&logoColor=white)
![Microservices](https://img.shields.io/badge/-Microservices-000000?style=flat-square&logo=cloud&logoColor=white)
![Eureka](https://img.shields.io/badge/-Eureka-E50914?style=flat-square&logo=Netflix&logoColor=white)


## Features
* Uses Spring Weblux and RabbitMQ to handle all requests while maintaining an asynchronous, non-blocking architecture with loose coupling between services.
* Reimbursement requests can be submitted with reimbursement rate determined by event type, adjusted for User's current balance when required.
* Request forms pass through a hierarchy of approvers, from the User's direct supervisor to their Department Head and then finally to the Benefits Coordinator.
* If the User's direct supervisor happens to be the Department Head, supervisor approval step is skipped so that the Department Head need only approve the request once.
* User can optionally upload .msg attachments granting pre-approval from either their direct supervisor or from the Department Head. The attachment gets stored in an S3 bucket
  and the relevant approval stage is automatically skipped.
* The User may optionally store an attachment related to the event in pdf, png, jpg, txt, or docx format and it will be stored in an S3 bucket.
* The User may cancel any request that has yet to be awarded. If the request was in Pending status, the pending balance will be restored to their annual allowance.

To-Do List:
* Unit testing
* Error handling, particularly with regards to the message queues
* Settle on date and time formats
* Remove attachments and delete the contents from S3
* Implement User validation to restrict requests only to authorized users
* Utilize the response messages defined in the SystemMessages.properties file
* Pull the user's username from the request header and possibly use it to fill out the parts of the form that relate to the user and not to the event itself
* Inbox Service is yet to be defined and will likely need to refactor communication between it and the Form Service

## Getting Started
1. Using your CLI tool, `cd` into the directory you want to store the project in.

2. Clone the repository using: `git clone git@github.com:TuitionReimbursementManagementSystem/FormService.git`

3. Configure your environment variables if you do not wish to use the provided defaults:
   * EUREKA_URL: The URL for the TRMS Discovery Service
   * AWS_USER: The AWS IAM username that has Keyspaces and S3 permissions
   * AWS_PASS: The password for the IAM User that has Keyspaces and S3 permissions

4. Create a Keyspace with the name: `trms` or choose one of your own by modifying the `CassandraConfig.java` file:
```
@Configuration
@EnableReactiveCassandraRepositories(basePackages = {"com.skillstorm.repositores"})
public class CassandraConfig {

    @Bean
    CqlSessionFactoryBean session() {
        CqlSessionFactoryBean cqlSessionFactory = new CqlSessionFactoryBean();
        DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
        cqlSessionFactory.setSessionBuilderConfigurer(sessionBuilder ->
              sessionBuilder.withConfigLoader(loader).withKeyspace("trms"));
        cqlSessionFactory.setKeyspaceName("trms");

        return cqlSessionFactory;
    }

    ...
}
```

5. The schema will be auto-generated when you run the program, but you can modify the auto-ddl statements by editing the `CassandraConfig.java`
   file in the `configs` package:
```
@Configuration
@EnableReactiveCassandraRepositories(basePackages = {"com.skillstorm.repositores"})
public class CassandraConfig {

    ...

    @Bean
    public SessionFactoryFactoryBean sessionFactory(CqlSession session, CassandraConverter converter) {
        SessionFactoryFactoryBean sessionFactory = new SessionFactoryFactoryBean();
        ((MappingCassandraConverter) converter).setUserTypeResolver(new SimpleUserTypeResolver(session));
        sessionFactory.setSession(session);
        sessionFactory.setConverter(converter);
        // Auto-ddl statement: CREATE, CREATE_IF_NOT_EXISTS, NONE:
        sessionFactory.setSchemaAction(SchemaAction.CREATE_IF_NOT_EXISTS);

        return sessionFactory;
    }

    ...  
}
```

6. Set up the Trust Store for AWS Keyspaces.
   #### Note: These commands do not work in Powershell! You will need to use a Bash terminal if using Windows:
   * `cd` into the `/src/main/resources` folder and run the following commands:
   * `curl https://certs.secureserver.net/repository/sf-class2-root.crt -O`
   * `openssl x509 -outform der -in sf-class2-root.crt -out temp_file.der`
   * `keytool -import -alias cassandra -keystore cassandra_truststore.jks -file temp_file.der`
   * Set the password to match the `truststore-password` set in the `application.conf` file: `p4ssw0rd`
   * Say `yes` to trust the certificate when prompted
  
7. Configure datastax in the `application.conf` file to set the `basic.contact-points` and the `local-datacenter` to the region where your Keyspace is located. By default,
   we have chosen `us-east-2`:
```
datastax-java-driver {

    basic.contact-points = [ "cassandra.us-east-2.amazonaws.com:9142"]
    basic.request.consistency = LOCAL_QUORUM
    advanced.auth-provider{
        class = PlainTextAuthProvider
        username = ${AWS_USER}
        password = ${AWS_PASS}
    }
    basic.load-balancing-policy {
        local-datacenter = "us-east-2"
    }

    advanced.ssl-engine-factory {
        class = DefaultSslEngineFactory
        truststore-path = "./src/main/resources/cassandra_truststore.jks"
        truststore-password = "p4ssw0rd"
    }
}
```


## Usage

1. Ideally all requests would be sent through the Gateway Service, which by default is configured for port `8125`. If sending requests to the Form Service server directly rather than through the Gateway Service we utilize port `8081`.
   This can be changed in the `/src/main/resources/application.yml` file:
```
{
  # Configure Netty server:
  server:
    port: 8081
}
```
2. While CRUD operations on the request form can be done independently, the approval processes rely on communication with the User Service. All microservices are configured to run on different ports by default and are connected to a
   cloud-hosted database, so you can still host the program locally but will need to run the Discovery Service, Gateway Service, User Service, Form Service, and Inbox Service simultaneously for full functionality.
3. Assuming you are hosting locally and sending requests through the Gateway Service, it functions as follows:

### Creating a new Reimbursement Request Form:
1. `POST` to `http://localhost:8125/forms`
2. With the request body:
```
{
    "username": "[String]",
    "firstName": "[String]",
    "lastName": "[String]",
    "email": "[String]",
    "time": "[HH:mm]",
    "date": "[YYYY-MM-DD]",
    "location": "[String]",
    "description": "[String]",
    "cost": [Double],
    "eventType": "[String]",
    "gradeFormat": "[String]",
    "justification": "[String]"
}
```
3. In addition to the above required fields, you may optionally include any of the following:
```
{
  "passingGrade": "[String]",
  "hoursMissed: [Integer]
}
```
4. If a `passingGrade` is not supplied, a default passing grade will be used depending on the event type.
5. `eventType` must be entered as a String that conforms to the `eventType` enum defined in the Form Service. To view a list of acceptable values: `GET http://localhost:8125/forms/events` to produce the following response:
```
[
    "UNIVERSITY_COURSE",
    "SEMINAR",
    "CERT_PREP_CLASS",
    "CERTIFICATION",
    "TECH_TRAINING",
    "OTHER"
]
```
6. `gradeFormat` must be entered as a String that conforms to the `gradeFormat` enum defined in the Form Service. To view a list of acceptable values: `GET http://localhost:8125/forms/grade-formats` to produce the following response:
```
[
    "SCORE",
    "PRESENTATION",
    "PASS_FAIL",
    "OTHER"
]
```
7. The `date` field must conform to a date that is at least 7 days from today or the request will not be accepted.
8. A successful response will take the following form:
```
{
    "id": "[UUID String]",
    "username": "[String]",
    "firstName": "[String]",
    "lastName": "[String]",
    "email": "[String]",
    "time": "[HH:mm]",
    "date": "[YYYY-MM-DD]",
    "urgent": [Boolean],
    "location": "[String]",
    "description": "[String]",
    "cost": [Double],
    "gradeFormat": "[String]",
    "passingGrade": "[String]",
    "eventType": "[String]",
    "justification": "[String]",
    "hoursMissed": [Integer],
    "attachment": null,
    "supervisorAttachment": null,
    "departmentHeadAttachment": null,
    "status": "CREATED",
    "reasonDenied": null,
    "excessFundsApproved": false,
    "reimbursement": [Double]
}
```

### Adding an Event-Related Attachment:
1. After the Request Form has been created, you may optionally add an event-related attachment prior to submitting it.
2. To do this, use the `id` returned in the response after creating the form and then `POST` to  `http://localhost:8125/forms/{id}/attachment` with a Body
  that is a file with extension: `.pdf`, `.png`, `.jpg`, `.jpeg`, `.txt`, or `.docx` along with a Header that includes the relevant `Content-Type`.

### Adding a Supervisor Pre-Approval Attachment:
1. After the Request Form has been created, you may optionally add a supervisor pre-approval attachment prior to submitting it to skip the supervisor approval step:
2. To do this, use the `id` returned in the response after creating the form and then `POST` to  `http://localhost:8125/forms/{id}/supervisor-attachment` with a Body
  that is a file with a `.msg` extension along with a Header that includes the relevant `Content-Type`.

### Adding a Department Head Pre-Approval Attachment:
1. After the Request Form has been created, you may optionally add a Department Head pre-approval attachment prior to submitting it to skip the Department Head approval step:
2. To do this, use the `id` returned in the response after creating the form and then `POST` to  `http://localhost:8125/forms/{id}/department-head-attachment` with a Body
  that is a file with a `.msg` extension along with a Header that includes the relevant `Content-Type`.

### Viewing the Event-Related Attachment:
1. After it has been uploaded, you may view the Event-Related Attachment associated with a Request Form by sending a `GET` request to the same URL that was used to upload it:
2. `GET http://localhost:8125/forms/{id}/attachment`

3. ### Viewing the Supervisor Pre-Approval Attachment:
1. After it has been uploaded, you may view the Supervisor Pre-Approval Attachment associated with a Request Form by sending a `GET` request to the same URL that was used to upload it:
2. `GET http://localhost:8125/forms/{id}/supervisor-attachment`

3. ### Viewing the Department Head Pre-Approval Attachment:
1. After it has been uploaded, you may view the Department Head Pre-Approval Attachment associated with a Request Form by sending a `GET` request to the same URL that was used to upload it:
2. `GET http://localhost:8125/forms/{id}/department-head-attachment`


## Contributors
* Quentin Hardwick

## License
This project uses the following license: <license_name>.
