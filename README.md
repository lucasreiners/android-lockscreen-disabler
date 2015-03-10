Android Lockscreen Disabler
=========

[![Build Status](http://www.griffenfeld.dk:8080/job/LockscreenDisabler/badge/icon)](http://www.griffenfeld.dk:8080/job/LockscreenDisabler/lastBuild/)

A module that simply hooks into the lockscreen's callback, wether there is a password required and mocks, that there is no password needed for unlocking the device. As the password is still active in the background, other apps (especially the apps that enforce such password policies) will never notice this little hack.

About
----

Sometimes you may face the problem that you are forced to setup a device pin or password everytime you want to unlock your phone.
There may be different reasons: some companies require such protections when you want to receive emails onto your mobile device.
Another public reason is: you want to use VPN on your Android smartphone. To setup a VPN you are forced by the system to setup some kind of security.

This module is for all the users who don't want to be forced to use a protection, although using the services descriped above.

Download
---
Download it from Xposed repository:<br>
http://repo.xposed.info/module/com.lr.keyguarddisabler

Support
---
You can get support at the corresponding XDA-thread<br>
http://forum.xda-developers.com/xposed/modules/mod-lockscreen-disabler-t2587192

Disclaimer
----
I am not responsible for any damages to you or your device, nor for any consequences that result from using this mod.
This mod is intended for testing purposes and not for bypassing security policies or guidelines in your everyday (work-)life.

License
----
Copyright 2015 LucasR93

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

