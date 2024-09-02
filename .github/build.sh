#!/bin/sh
export DISPLAY=:99
sudo Xvfb -ac :99 -screen 0 1280x1024x24 > /dev/null 2>&1 &
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/main/ci-build.sh
sh ci-build.sh
