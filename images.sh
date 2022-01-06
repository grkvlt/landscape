#!/opt/local/bin/bash
#
# Generate Fractal Landscape Images
#
# Copyright 2022 by Andrew Donald Kennedy

#set -x # DEBUG

# Setup config variables
VERSION="0.5-SNAPSHOT"
IMAGES_OPTS="-Dimages.color -Dimages.save.all=false"
MAX="10"
ROUGH="2"

# Run generation loop
while [ ${i:-0} -lt ${MAX} ] ; do
    java ${IMGES_OPTS} -jar target/landscape-${VERSION}.jar ${ROUGH}
    i=$(($i + 1))
done

