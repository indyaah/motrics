## motrics : (Mock + Metrics)

Goal: Receive and aggregate multiple metrics in memory efficiently and serve the aggregation on demand over ReST API.

Features:
1. Create new metrics on-demand
2. Query a single metrics;
    1. All aggreated values (a-e below)
    2. One of the following values
        1. Minimum
        2. Maximum
        3. Mean (Arithmetic)
        4. Average
        5. Sample Count

#### Stack

* `JDK`: `8`
* `Spring Boot`: `1.5.11`
* `Docker`

#### Local Setup
* Your favourite IDE (IntelliJ/Eclipse)
* Maven (Wrapper provided)
* Docker