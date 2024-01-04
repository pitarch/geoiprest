# geoip-rest

REST API to translate an IP into a geo location implemented in scala using http4s.

## dbip-city-ipv4.csv

The file `dbip-city-ipv4.csv` is a copy of the [dbip-city-ipv4.csv](https://db-ip.com/db/download/city) file from [db-ip.com](https://db-ip.com/).

The format of the file is:

- First IP address
- Last IP address
- Country ISO-3166-alpha2 code
- State or Province name
- empty
- City name
- Approx. Latitude
- Approx. Longitude

Example:

```csv
31.31.91.0,31.31.91.255,ES,Valencia,,Port de Sagunt,,39.6621,-0.228449,
```

