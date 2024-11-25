- # General
    - #### Team#: 50 (team beef)

    - #### Names: Mingkun Liu, Feiyang Jin

    - #### Project 5 Video Demo Link: https://youtu.be/Sc6U4CiJ5YM

    - #### Collaborations and Work Distribution: 
        - #### Mingkun Liu:
          - Implemented full-text search and autocomplete functionality, ensuring user-friendly search with efficient query processing and caching.
            Integrated autocomplete with arrow key navigation, "Enter" key handling, and a dropdown suggestion list limited to 10 items.
            Configured and tested load balancing with Apache2, enabling sticky sessions and routing traffic to master/slave Tomcat instances.
        - #### Feiyang Jin: 
          - Enabled JDBC connection pooling across all servlets, optimizing database connection management with prepared statements.
          Set up MySQL master-slave replication on AWS, ensuring proper data propagation and handling read/write separation.
          Developed and integrated fuzzy search functionality using LIKE and edth, improving query accuracy for misspelled searches.


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - The configuration file is WebContent/META-INF/context.xml, and the connections are organized by class DbService, 
    - and all the servlets that use database.

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - We created 2 datasources with connection pooling set up, both of them are organized by class DbService, and all the servlets code get connection through db service.

    - #### Explain how Connection Pooling works with two backend SQL.
    - We created 2 datasources for each database, and randomly give one when read, give master when write. 


- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - WebContent/META-INF/context.xml
    - #### How read/write requests were routed to Master/Slave SQL?
    - By using DbService class function, read request will randomly get master/slave connections, and write wil only get master connections.
