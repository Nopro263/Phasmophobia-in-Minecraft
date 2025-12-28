# Phasmophobia in Minecraft

## About this Project

The goal of this Project is to create a functional Minecraft Java Server that recreates the Gameplay
from [Phasmophobia](https://store.steampowered.com/app/739630/Phasmophobia/). \
Because the Vanilla features would be annoying to remove, I've decided to use [Minestom](https://minestom.net/) to allow
me to create my server from the ground up.

## The Goal

I want to implement all the equipment from Phasmophobia and core gameplay mechanics. \
I will not implement all 14 available maps nor all 27 individual ghosts, as that would be far too much work designing
the different parts. \ I might reconsider this when every gameplay element has been implemented.

## What is Phasmophobia?

If you've never heard of this game before you might be confused, so let me clear some things for you: \
Phasmophobia is a 1-4 player psychological horror game where you are a team of paranormal investigators and have been
tasked to identify the ghost that possesses a specific house/location. \
In order to determine the ghost type, you have to find specific evidence that points to a type. \
For example a ghost of type "Banshee" will have the evidence "Ultraviolet", "Ghost Orb" and "D.O.T.S Projector". \
Read more [here](https://store.steampowered.com/app/739630/Phasmophobia/)

## Running the Server

> [!WARNING]
> This server has been designed to run on linux, I don't think it will run on windows

### install the required dependencies

This project depends on three libraries and on `java-25`

- [VirtualGL](https://www.virtualgl.org/) in order to run on servers without a gpu (not required if you have a gpu)
- Xvfb to render the Minecraft client onto a virtual framebuffer
- [HeadlessMC](https://github.com/headlesshq/headlessmc) to run a Minecraft client on the server

Install `Xvfb` via `apt` and `HeadlessMc` so that you have a `headlessmc-launcher` somewhere

### build the jar

clone this repo

```shell
git clone https://github.com/Nopro263/Phasmophobia-in-Minecraft
```

build using maven

```shell
mvn clean package
```

and copy the resulting jar somewhere else

### initialize the configuration

```shell
java -jar`./PhasmoInMc.jar
```

it will create a `config.yml` file and immediately exit \
in the `camera` section change the `headlessmcPath` to point to the directory with `headlessmc-launcher`.
In the headlessmc directory, there should be a `HeadlessMC` directory, cd into it
open the `config.properties` file and insert the following:

```properties
hmc.check.xvfb=true
hmc.offline=true
hmc.offline.username=CAM
hmc.offline.uuid=22689332a7fd41919600b0fe1135ee34
```

If you change the username of the uuid, make sure to update them in the `config.yml` as well.

I would recommend enabling the `devMode` in order to get all commands

> [!CAUTION]
> the voicechat is currently broken, please disable it

### start the server

make sure to set the `DISPlAY` environment variable (if unsure, just set it to `:99`)

```shell
export DISPLAY=:99
```

then start the jar

```shell
java -jar`./PhasmoInMc.jar
```

> [!NOTE]  
> currently you have to use your own resource pack (if you want one), in the future I will include it in the jar

the server will now listen on the port specified in `config.yml` and you can join on version `1.21.10`