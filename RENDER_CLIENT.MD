https://github.com/3arthqu4ke/headlessmc
xvfb
https://www.virtualgl.org/

config:
hmc.check.xvfb=true \
hmc.offline=true \
hmc.offline.username=CAM

Xvfb :1 -screen 0 1024x768x24 +extension GLX +render -noreset &
env DISPLAY=:1 vglrun ./headlessmc-launcher --command launch fabric:1.21.8