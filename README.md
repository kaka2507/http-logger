# http-logger
HTTP Request and Response Logger Library for Spring Boot Projects

# Demo
![alt text](https://raw.githubusercontent.com/vcoder4c/http-logger/master/screenshot/screenshot_v0.0.5.png)

# How to Use
1. I have already published this library to Maven Central Repo, so you can integrate it to your library very easily. Please visit [this place](https://mvnrepository.com/artifact/me.vcoder/http-logger/0.0.5) for integrating.
2. By the default, the Spring Boot will not scan all components which is outside of the default package. So, we need to specify it as blow
- If you using the default annotation @SpringBootApplication --> just attach "me.vcoder" on the scanBasePackages property.
- When running the Spring Boot application, the filter httpLoggerFilter should be logged on the filter chain. 

3. There are three configuration for this library, using @Value annotation
- "http.logger.enable" (default is true), set false in case you want to hide the log, and it will not affect your application performance.
- "http.logger.filter.include" and "http.logger.filter.exclude" is a regex list, which let the filter should log the request or not. Note: include is higher priority than exclude. For example: to exclude on resources request on your resource directory, you should specify the exclude config as below:
```
    http.logger.filter=^/js/.*, ^/css/.*, ^/images/.*
```
# TO DO LIST
. v0.0.5
- [x] Replace two old filters by one filter only to make sure request and response can be logged at the same time.
- [x] Log Request Body
- [x] Log Response Body
- [x] Exclude request with regex
- [x] Disable log (default is enable)

Please create issues for what you think this library should implement. Thanks

