FROM sirlatrom/android:19

ENV HOME /root
ENV LD_LIBRARY_PATH ${ANDROID_HOME}/tools/lib

RUN echo no | android create avd --force -n test -t android-19

WORKDIR /workspace
ADD ./gradlew /workspace/gradlew
ADD ./gradle /workspace/gradle
RUN ./gradlew

ADD . /workspace

RUN /workspace/gradlew assembleDebugTest

ADD start-emulator    /usr/local/bin/
ADD wait-for-emulator /usr/local/bin/

CMD start-emulator "/workspace/gradlew connectedAndroidTest"
