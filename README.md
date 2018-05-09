# devoptics-connect
Atlassian connect app for devoptics.

## Run instructions
To run this locally:
Open two terminal windows:
In the first terminal, run:
```
ngrok http 8087
```
Adjust the port as needed if you have a conflict locally. This will bring up the ngrok tunnel and display a screen similar to below

```
Session Status                online                                                                                                                                                                                           
Session Expires               7 hours, 58 minutes                                                                                                                                                                              
Version                       2.2.8                                                                                                                                                                                            
Region                        United States (us)                                                                                                                                                                               
Web Interface                 http://127.0.0.1:4040                                                                                                                                                                            
Forwarding                    http://384949f2.ngrok.io -> localhost:8087                                                                                                                                                       
Forwarding                    https://384949f2.ngrok.io -> localhost:8087                                                                                                                                                      
                                                                                                                                                                                                                               
Connections                   ttl     opn     rt1     rt5     p50     p90                                                                                                                                                      
                              0       0       0.00    0.00    0.00    0.00
```

Update src/main/resources/application.yml with the secure url of the tunnel. We will have to fix this obviously but is has been good enough for a POC. Then in the second the terminal, run the commands below, replacing FORWARDING_URL with the same (https://384949f2.ngrok.io in the example above).
```
mvn clean install -U -e
mvn spring-boot:run -Drun.arguments="--addon.base-url=FORWARDING_URL"
```

Once the app starts the url above will serve serving src/main/resources/atlassian-connect.json. Once you have the application running you can use FORWARDING_URL/atlassian-connect.json to add the connect app to your JIRA Cloud instance.