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
* Your favourite IDE - Wont work with `IntelliJ IDEA 2018.1` due to Lombok plugin issue. Non released/patched plugin can be installed from disc though, [more here](https://github.com/mplushnikov/lombok-intellij-plugin/issues/468#issuecomment-377436538)
* Maven `3.5.3` (Wrapper provided)
* Docker (technically any moderately recent version should be fine)

#### Building

```bash
./mvnw clean install
```

It will generate docker image named `motrics-1.0.0:latest` which could be run for testing.

#### API Docs 
Project contains Swagger based API Documentation which could be accessed at http://localhost:8080/swagger-ui.html

#### Time Complexities of various ops
* `O(1)` for `Creating new Metric`
    - Constant time look up from a (Concurrent) Map
* `O(1)` for `Reading any/all Summaries for a give metric`
    - Constant time look up from a (Concurrent) Map and then POJO field access
* `O(log N)` for `Insert inserting new datapoint for a given metric` 
    - `O(log N)` - insertion time complexity of Heaps due to underlying Priority Queues
    - `O(1)` - during balancing of the heaps
 


#### Space Complexities of various ops
* `O(n)` for `Storing Metrics`
    - Both storage and look up maps contains same amount of elements as metrics
* `O(n)` for `Storing all summaries exception for Median`
    - All summaries exception median are derived values and not raw/golden data (i.e all the data points)
* `O(kn)` for `Storing median` 
    - `N` is number of metrics
    - `k` is number of datapoints (items stored in priority queues)

###### This (`O(kn)` space complexity for `Median`) could be theoratically improved by using probabilistic algos to estimate value i.e - https://link.springer.com/chapter/10.1007%2F978-3-642-40273-9_7 or https://research.neustar.biz/2013/09/16/sketch-of-the-day-frugal-streaming/
  
#### Examples
1. Save/Create new entry 
```bash
curl -X POST \
  http://localhost:8080/metric \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "soma_metric"
      }' 
```

2. Add value to a metric
```bash
curl -X PUT \
  'http://localhost:8080/metric' \
  -H 'Accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br'
  -d '{
        "id": "<VALID_UUID>",
        "value": <VALID_DOUBLE>
      }' 
```

3. Find all summaries for given metric
```bash
curl -X GET \
  'http://localhost:8080/metric/<VALID_UUID>' \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br' \
```
4. Find min for given metric
```bash
curl -X GET \
  'http://localhost:8080/metric/<VALID_UUID>/min' \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br'
```

5. Find max for given metric
```bash
curl -X GET \
  'http://localhost:8080/metric/<VALID_UUID>/max' \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br'
```

6. Find average for given metric
```bash
curl -X GET \
  'http://localhost:8080/metric/<VALID_UUID>/avg' \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br'
```

7. Find median for given metric
```bash
curl -X GET \
  'http://localhost:8080/metric/<VALID_UUID>/med' \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br'
```

8. Find sample count for given metric
```bash
curl -X GET \
  'http://localhost:8080/metric/<VALID_UUID>/count' \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate, br'
```

