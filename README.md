# DbTradeAlert for Android
DbTradeAlert alerts you to securities reaching a specified price or at a specified date. 

This post consists of a Quick Start followed by a description of the app’s Screens and a Glossary.

## Quick Start

### Workflow
DbTradeAlert comes with some predefined securites and watchlists. You can use them to try DbTradeAlert or simply remove them.

After that add the securities you are interested in:

1. In the main screen’s overflow menu tap Manage Securities
1. In the Manage Securities screen tap New
1. In the Edit Security screen type in the security’s symbol. As the quotes are delivered by Yahoo you need to specify a symbol that Yahoo supports. The easiest way to find a symbol is to open http://finance.yahoo.com/ and use the Quote lookup field.

Additionally you can specify data like a base price as well as a lower, upper, or trailing target. Then add the security to one or more watchlists which you create similar to adding securities.

Tap OK in the Edit Security screen and close the Manage Securities screen to go back to the main screen. Then tap the Refresh button in the top right corner to update the reports. If any of the specified targets are hit you will also get a notification.

You can also tell DbTradeAlert to remind you of a specific date. For this go to the Edit Reminder screen like described above for securities and type in the reminder’s date, heading and optionally a note. Also activate the reminder and finally tap OK.

DbTradeAlert will get quotes for all securities about every hour even when the app is closed and notify you about targets that are hit. At the same time it will evaluate reminders.

At any time you can add, edit and remove securities as well as add them to or remove them from watchlists. You can also add, edit and remove watchlists as well as reminders.

### Backup
On Android Marshmallow the app’s settings and database will be backed up automatically if you opted for backups when creating your Google account. You can also copy the database to a computer:

1. Connect your device and computer by USB
1. On the device’s USB notification select the Transfer Files (MTP) option
1. Make shure DbTradeAlert doesn’t write to the database – check the last update’s timestamp or halt the automatic quote updates temporarily in settings
1. In DbTradeAlert’s menu select Copy Database | Export Database – a Toast shows how many bytes were copied and to which directory
1. With your computer’s Explorer navigate to the phone’s Internal Storage\Android\data\de.dbremes.dbtradealert\files folder
1. Copy dbtradealert.db to a folder on your computer – the database is readonly at it’s current location

To copy the database back to your device:

1. Copy the database to the phone’s Internal Storage\Android\data\de.dbremes.dbtradealert\files folder on your computer
1. Make shure DbTradeAlert doesn’t write to the database
1. In DbTradeAlert’s menu select Copy Database | Import Database – a Toast shows how many bytes were copied and to which directory

Please note that Android will automatically delete the database copy in internal storage when you uninstall DbTradeAlert. If you want to keep a copy save it to your computer.

### Target Group
DbTradeAlert is meant for people who check their portfolio maybe once a week. If your portfolio needs more attention you probably need a more sophisticated app. It’s also butlerlike discreet: neither the database nor the UI including notifications provide any clues about your portfolio’s worth – or whether you have a portfolio at all.

## Screens

### Edit Reminder Screen
You use the Edit Reminder screen to add or change reminders. At the specified date the specified heading is shown if the reminder is active.

To access the Edit Reminder screen:

1. In the app’s overflow menu select Manage Reminders
1. In the Manage Reminders screen tap New or tap Edit on an existing reminder

### Edit Security Screen
You use the Edit Security screen to add or change securities.
The security’s symbol can only be specified when adding a security.
In addition to the symbol you can provide information like

* base price: will show in the report’s chart
* base price date: doesn’t show up in DbTradeAlert anywhere else
* maximum price: will show in the report’s chart
* maximum price date: doesn’t show up in DbTradeAlert anywhere else
* targets: when triggered they show up in the report and you get a notification
* lower target
* upper target
* trailing target (trailing stop, in %)

To remove the base or maximum prices, their dates, or a target clear the respective field.

You will also want to add the security to a watchlist.

The security’s name and currency symbol come with the quotes so you don’t need to provide them.

To access the Edit Security screen:

1. In the app’s overflow menu select Manage Securities
1. In the Manage Securities screen tap New or tap Edit on an existing security

You can also access the Edit Security screen by long-tapping a security in the app’s main screen.

### Edit Watchlist Screen
You use the Edit Watchlist screen to add or change watchlists. Here you can edit a watchlist’s name as well as add securities to it or remove them from it.

To access the Edit Watchlist screen:

1. In the app’s overflow menu select Manage Watchlists
1. In the Manage Watchlists screen tap New or tap Edit on an existing watchlist

### Manage Reminders Screen

In the Manage Reminders screen you can delete reminders as well as start the Edit Reminder screen.

To access the Manage Reminders screen select Manage Reminders in the app’s overflow menu.

### Manage Securities Screen
In the Manage Securities screen you can delete securities as well as start the Edit Security screen.

To access the Manage Securities screen select Manage Securities in the app’s overflow menu.

### Manage Watchlists Screen
In the Manage Watchlists screen you can delete watchlists as well as start the Edit Watchlist screen.

To access the Manage Watchlists screen select Manage Watchlists in the app’s overflow menu.

### Settings Screen
In the Settings screen you specify your preferences:

* Disable quote downloads to temporarily save download costs and interruptions
* At which times DbTradeAlert should not download quotes (after hours, weekends) to save download costs

Gaps in the settings for business days and hours have no effect. DbTradeAlert uses the first and last day / hour of the setting to decide whether it should auto refresh.

To access the Settings screen select Settings in the app’s overflow menu.

### Trade Alert Window
The trade alerts window contains a line for each triggered signal and each due reminder. Tapping the window will take you to the main screen.

Example for a trade alert based on a target: “NOVN.VX: low = 79.55; T = 92.07” means that Novartis named shares have a trailing target coresponding to 92.07 (CHF) which is hit because the day’s low was at 79.55 (CHF). The trade alert window will have one line like this for each signal.

Each due reminder will show its heading as a line. And if the window shows one or more reminder-related trade alerts it will also have a Reminders button. Tapping that button will take you to the Manage Reminders screen.

### Watchlist Screen
The Watchlist screen is the app’s main screen and shows reports for all securities in a watchlist. Watchlists are ordered by name.

Tap and hold a report to go to the Edit Security screen. Swipe right or left to go to the next or previous watchlist.

The top of the screen shows the timestamp of the last update and provides access to the Refresh button as well as to the app’s menu.

## Glossary

### Base price
The base price is a price you want to remember like the price you paid for a security. You can edit it in the Edit Security screen. DbTradeAlert shows this price in a watchlist’s reports.

### Base price date
The base price date is a date you want to remember like the date you bought a security. You can edit it in the Edit Security screen. This date is for your information only and not used by DbTradeAlert.

### Maximum price
When you add a security DbTradeAlert will automatically start recording the highest price. Only if you want to use a historical price that was reached before you added the security you need to specify it in the Edit Security screen. DbTradeAlert shows this price in a watchlist’s reports.

### Maximum price date
The maximum price date is the date on which the security’s maximum price was reached. Like the maximum price its date is updated automatically. This date is for your information only and not used by DbTradeAlert.

### Overflow Menu
The overflow menu is depicted by three vertically ordered dots in the upper right corner of the app’s main screen. You can access all the app’s management screens from this menu.

### Quote
A quote is information about a security that DbTradeAlert aquires online every hour or upon manual request. DbTradeAlert shows this information in its main screen for each security in a watchlist. Quote data is updated even when a security is in no watchlist.
Note that quotes will occasionally have missing or incorrectly formatted values which DbTradeAlert will ignore. If the value for last trade (current price) is missing or incorrectly formatted the whole quote will be ignored.

### Refresh
Refresh means to download quotes, evaluate targets and reminders, and update reports and notifications. By default DbTradeAlert does that once an hour but you can tap the main screen’s Refresh button at any time. Auto refresh is limited to exchange’s opening hours to save battery and network usage. The default values for those opening hours are monday to friday from 9 am to 7 pm. You can change the opening hours and whether DbTradeAlert auto refreshes at all in its Settings screen. Consider auto refresh’s delay of up to 1 hour and timezone differences when you set opening hours.

### Reminder
You create a reminder for a date you want to act on. That could be a bond’s maturity date or a date where the issuer can call the bond. But you don’t directly connect a reminder to a security.

When the date is reached and the reminder is active its heading will show in a trade alert like those for signals.

### Report
Watchlists show a report for each security added to them. A report’s upper part shows these values (from left to right and top to bottom):

* Name
* Signals triggered:
  * L: lower target hit
  * T: trailing target hit
  * U: upper target hit
  
  Targets are evaluated in the order shown above and only the last of the resulting signals will show.
  If a trailing target is specified the signals field will always show an underscore.
* Percent changed from last day’s closing to current price
* Symbol
* Last trade (current price)
* Timestamp of last trade
* Percentage changed from maximum price
* Percentage of average volume traded today

If a quote is older than 24 hours its timestamp and signals will have a sepia background color. Missing values will be shown as hyphens.

A report’s lower part provides two charts:

* The upper chart shows a line visualizing the security’s quote data (open, ask and bid, current price, day’s high and low, previous close)
  * The current price is shown above the line
  * All other data show up as markers (O, a and b, H and L, P) below the line
  * The spread between ask and bid is emphasized by a black rectangle
  * All reports for quote data in a watchlist share the same scale relative to last price – so a 10 % difference will be the same distance in each quote chart
* The lower chart shows a line visualizing your targets for that security (base price, lower and upper target, and trailing target – if specified)
  * Again, the current price is shown above the line
  * All other data show up as markers (B, L and U, and T) below the line
  * If a base price has been entered the area between it and current price is emphasized by a green (base price < current price) or red (base price > current price) rectangle – not intended for short positions!
  * All reports for target data in a watchlist share the same scale relative to last price – so a 10 % difference will be the same distance in each target chart
Security

A security can be any financial instrument like stocks, bonds, currency pairs, funds or indexes. It’s identified by a security symbol and edited in the Edit Security screen. To add a new security use the Manage Securities screen.

### Signal
When a target is hit it triggers a signal which shows up in the security’s report. You’ll also get a trade alert which will contain a line for each signal.

### Symbol
A symbol identifies not only the security but also the exchange – for example NESN.VX means Nestle AG registered shares at SIX Swiss Exchange. Symbols have to be unique in DbTradeAlert. As the quotes are delivered by Yahoo the symbol needs to be supported by Yahoo.

### Target
A target is a condition you want to act upon. When a target is reached it triggers a signal. Reminding you of those is DbTradeAlerts main purpose. While your bank or broker provide stop loss orders and the like you probably won’t want to use them for low-volume securities especially if they trigger market orders.

Targets and resulting signals are shown in each report of a watchlist. If one or more signals are triggered you’ll also receive a trade alert.

You specify targets in the Edit Security screen.

### Trade Alert
A trade alert is the thing that gave DbTradeAlert its name. If a target is hit or a reminder is due you’ll receive a trade alert. Trade alerts use an Android feature that consists of a small window which shows one or more notifications for an app and is usually accompanied by a sound and optionally vibration. DbTradeAlert issues trade alerts for all its signals and due reminders directly after a manual or scheduled refresh.

### Trailing Target
A trailing target a.k.a. trailing stop or trailing stop loss trails the security’s maximum price. For example if you specify a trailing target of 10 % in the Edit Security screen and the security’s maximum price is 100 EUR the signal will be triggered when the security’s price drops to 90 EUR or below. But if the security’s maximum price rises to 110 EUR the trailing target of 10 % will equal 99 EUR. So a trailing target simply avoids having to constantly adjust a lower target.

### Watchlist
A watchlist is a named list of reports on securities ordered by their names. A security can be in any number of watchlists. It can also be in no watchlist at all for example if you want to keep the notes and targets but don’t currently follow the security.