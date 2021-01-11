#!/opt/local/bin/bash
#
# Render MP4 of Fractal Landscape Fly-over
#
# Copyright 2020-2021 by Andrew Donald Kennedy

# Setup config variables
FILE="flyover"
DIR="./flyover"
#RESOLUTION="1930x1200"
RESOLUTION="1024x512"
FPS="15"
VERSION="0.4"

# Determine output video filename
last=$(ls -1 ${FILE}-*.mp4 | cut -d- -f2 | cut -d. -f1 | sort | tail -1)
next=$(printf "%02d" $((${last} + 1)))
video="${FILE}-${next}.mp4"

# Execute Java code to render landscape frames if required
if [ ! -f "${DIR}/frame-0000.png" ] ; then
    echo "- Generating PNG landscape files"
    time java -cp ./target/landscape-${VERSION}.jar landscape.FlyOver false
else
    echo "- Landscape PNG files already rendered"
fi

# Execute ffmpeg to compile frames into animation
echo "- Compiling ${video} animation"
cd ${DIR}
time ffmpeg -r ${FPS} \
	-f image2 \
	-s ${RESOLUTION} \
	-i frame-%04d.png \
	-vcodec libx264 \
	-crf 10 \
	-pix_fmt yuv420p \
	../${video}

