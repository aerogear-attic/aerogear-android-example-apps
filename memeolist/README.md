# Memeolist

Memeolist is an example app to show how to integrate an Android app with [AeroGear Data Sync](https://github.com/aerogear/data-sync-server) using the [AeroGear Android SDK](https://github.com/aerogear/aerogear-android-sdk). 

## How to run it

1. Make sure you have a [AeroGear Data Sync Server](https://github.com/aerogear/data-sync-server) instance up and running
2. Update the sync url in `app/src/main/assets/mobile-services.json`

## How it works

### User/Profile

The login feature is provided by [Keycloak](https://www.keycloak.org/). After you login/create your user the app will create a profile in the [AeroGear Data Sync Server](https://github.com/aerogear/data-sync-server) based in your [Keycloak](https://www.keycloak.org/) profile.

### Meme

To create a meme the app will upload the selected image from the app to [imgur.com](http://imgur.com), generate a meme (and a public link) using [memegen](https://memegen.link) and store it on the [AeroGear Data Sync Server](https://github.com/aerogear/data-sync-server).

## Version matrix

| memeolist | Android SDK | AeroGear Data Sync Server  |
|:---------:|:-----------:|:--------------------------:|
| [2.1.0-alpha.1](https://github.com/aerogear/aerogear-android-example-apps/releases/tag/2.1.0-alpha.1) | [2.1.0-alpha.1](https://github.com/aerogear/aerogear-android-sdk/releases/tag/2.1.0-alpha.1) | [0.1.0-alpha](https://github.com/aerogear/data-sync-server/releases/tag/0.1.0-alpha) |

:point_right: master branch contains development changes that may not work with the released server. If you wanna play with the master branch you will need to build the [AeroGear Android SDK](https://github.com/aerogear/aerogear-android-sdk) snapshot dependency yourself. Please use master only if you are contributing to the application.
