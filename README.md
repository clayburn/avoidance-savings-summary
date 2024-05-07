# avoidance-savings-summary

To build:

```shell
./gradlew installDist
```

To run:

```shell
export API_KEY=your-api-key
build/install/avoidance-savings-summary/bin/avoidance-savings-summary --develocity-url=https://dv.example.com --days=7 --csv=build/avoidance-savings-summary.csv 
```