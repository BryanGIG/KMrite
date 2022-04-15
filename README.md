# KMrite
Patch runtime process library(*.so) with offset & hex bytes
<br>
This project is made for you who like to build and injector app with no c++ at all.

## Build & Usages 
- Download this project
    - `git --clone  https://github.com/BryanGIG/KMrite`
- Open and build the project
- Install the app into your android devices
    - If you had root, grant permission root
    - If you non-root, use virtual app
- Open app that you want to inject
- Open the KMrite
- Fill all needed information :
    - Package Name : **The package of your process**
    - Lib Name : **Library(*.so) name**
    - Offset : **0x...**
    - Hex : **0A010213**
- Click **START PATCH** to begin inject into the process

## Credits
- [LibInjector](https://github.com/jbro129/LibInjector) by [jbro129](https://github.com/jbro129)
- [libsu](https://github.com/topjohnwu/libsu) by [topjohnwu](https://github.com/topjohnwu) 
