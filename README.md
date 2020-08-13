# EasyTrips 

An Android app to allow the end-users to book fully customized trips at unknown cities without hassle.

Get the APK [here](https://drive.google.com/file/d/1vp2bjoXsZiKlEC1wezLmXrSHf0VK1GCo/view?usp=sharing).

## Screenshots

![alt text](https://github.com/ishans996/EasyTrips/blob/master/easytrips_images/Group%201.png)

## Installation
Clone this repository and import into **Android Studio**
```bash
git clone https://github.com/ishans996/EasyTrips.git
```

## Configuration

Grab a [Google API Key](https://developers.google.com/maps/documentation/javascript/get-api-key).
Search for the placeholder ```API_KEY``` in the following files and replace it with your api key
- ```/app/src/main/res/values/google_maps_key.xml``` 
- ```/app/src/main/res/values/strings.xml``` 

## Generating signed APK
From Android Studio:
1. ***Build*** menu
2. ***Generate Signed APK...***
3. Fill in the keystore information *(you only need to do this once manually and then let Android Studio remember it)*

## Maintainers
This project is mantained by:
* [Eeshan Deo](https://github.com/eeshan-d)
* [Ishan Sharma](https://github.com/ishans996)


## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Push your branch (git push origin my-new-feature)
5. Create a new Pull Request

## License

EasyTrips is [GNU GPL3 licensed](https://github.com/ishans996/EasyTrips/blob/master/LICENSE).
