#!/opt/local/bin/bash
#
# Render MP4 of Fractal Landscape Fly-Over
#
# Copyright 2020-2022 by Andrew Donald Kennedy

#set -x # DEBUG

# Setup config variables
NAME="flyover"
DIR="${HOME}/Landscape"
#RESOLUTION="1930x1200"
RESOLUTION="1024x512"
FPS="15"
VERSION="0.5-SNAPSHOT"

# Create directory
target="${DIR}/${NAME}"
mkdir -p ${target}

# Determine output video filename
last=$(ls -1 ${target}-*.mp4 | cut -d- -f2 | cut -d. -f1 | sort | tail -1)
next=$(printf "%02d" $((${last} + 1)))
video="${target}-${next}.mp4"

# Execute Java code to render landscape frames
if [ ! -f "${target}/frame-0000.png" ] ; then
    echo "- Generating PNG landscape files"
    time java -cp ./target/landscape-${VERSION}.jar landscape.FlyOver false
else
    echo "- Landscape PNG files already rendered"
fi

# Execute ffmpeg to compile frames into animation
echo "- Compiling ${video} animation"
cd ${target}
time ffmpeg -r ${FPS} \
	-f image2 \
	-s ${RESOLUTION} \
	-i frame-%04d.png \
	-vcodec libx264 \
	-crf 10 \
	-pix_fmt yuv420p \
	${video}

