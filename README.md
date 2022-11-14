# Gamma Portals

![Logo](/images/logo.png)

## Index

- [About](#about);
- [Config](#config);
- [Download](#download);
- [Installation](#installation).

## About

This plugin adds survival-buildable portals.
Portals can be built of iron blocks:

![Iron](/images/iron.png)

copper blocks:

![Copper](/images/copper.png)

golden blocks:

![Golden](/images/golden.png)

emerald blocks:

![Emerald](/images/emerald.png)

diamond blocks:

![Diamond](/images/diamond.png)

netherite blocks:

![Netherite](/images/netherite.png)

or even of listed above blocks mix:

![Mixed.png](/images/mixed.png)

Portals can have any vertical shape but number of inner blocks must not exceed
specified in the `config.yml` number of `max-blocks`.

To ignite portal left click on inner frame of portal with _Eye of Ender_.

To activate and link two portals you must place sign with same _descriptor line_
on both portals. One portals can be linked only with one another portal.
_Descriptor line_ must begin with `[` proceed with _tag_ and end with `]`.
_Tag_ can contain any english letter, number, and any punctuation character.

Here is an example of linking two portals:

![First sign](/images/first-sign.png)

![Second sign](/images/second-sign.png)

## Config

Configuration file has only two entries described below:

| Entry        | Type   | Description                                           |
|--------------|--------|-------------------------------------------------------|
| `max-blocks` | `int`  | Maximum number of inner portal blocks                 |
| `auto-save`  | `int`  | Number of seconds between automatic portal data saves |

## Download

Go to [releases](https://github.com/Maksim2498/mc-gamma-portals/releases)
section and choose desired version to download.

## Installation

Simply put downloaded .jar file to the plugins folder.
