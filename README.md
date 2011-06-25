# Cover art viewer - Overview

Shows a grid of cover art of all albums sourced from SqueezeboxServer.
Allows you to play them on a connected SqueezeboxTouch player via right click menu, choose album or track.
Trying to bring a more visual experience akin to browsing a shelf of cds to SqueezeBox.

![Screenshot](https://github.com/scaphe/SqueezeCoverGrid/blob/master/sqz-cav1.png"Screenshot")

# Build/Install

Note that this application comes with absolutely no guarentee, other than, I use it.

## MacOS

Build (mac application in directory macPkg, copy to where you like - thanks to http://www.informagen.com/JarBundler/):

    > ant clean dist macOS

Run:

    Now use finder to navigate to macPkg directory, should be an application with grid icon there

Notes:

If you don't want to build the code youself then:

    click on Downloads (top right of page),
    download the SqzCoverGrid.zip file,
    double click to expand it to show the built application,
    double click application (with a grid icon) to start

If you want to debug then follow the Linux directions

## Linux
From shell

Build:

    > ant clean dist

Run:

    > ./cav.sh


## Windows (untested)
From command window

Build:

    > ant clean dist

Run

    > .\cav.bat


# User Guide

On startup, if you are running the gui on a different machine than the SBServer then you will be prompted for the hostname of the server.

The main area of the screen shows a **grid** of the cover art of all albums found on the SqueezeBoxServer you specified.
Under this area should be a separator bar and a grid/list of all albums that you have not yet arranged (the new shelf).
Albums can be dragged off this shelf onto the grid immediately.   
If you wish to **re-arrange** the covers later you must select the "Edit positions" checkbox (top right of grid) to enter edit mode.
In edit mode you can also change the grid **icon size**.

To **playback** you can right click (popup menu) to queue album (select album name) or track (select track name).
For immediate playback hold shift key when right clicking.

To **search** start typing album or artist name into the search box at top right, all non-matching albums will be grayed out.
If you have no matches then no albums will be grayed out.  Press escape to clear search.  Ctrl+F takes focus to search.

Basic control over the attached SqueezeBox Player (e.g. Touch) is available with buttons under Search (the X means clear playlist).

The menu allows you to **refresh** from server, either full or just an update.  Note that the application caches all cover art
in your home directory (for startup speed) so a full refresh is a lot slower.
If  you just want to force-update a single album then in edit mode you can right click "Remove" which will make the gui forget that album.
The gui will check for updates (checks if number of albums on server changes) once every 5 minutes automatically, if finds changes then
will do an update.

You can **hide** albums that you don't want on the grid.  While in edit mode, right click, choose hide.  This will put the cover
on another "shelf" under the new shelf.  If you want the album back on the grid  you can just drag it back on.
This hidden shelf is only visible in edit mode.


# Thanks / Credits

Thanks to [javaslimserver](http://code.google.com/p/javaslimserver) for the java api to the server, without which I wouldn't even have attempted this.

Thanks to [JarBundler](http://www.informagen.com/JarBundler) for writing an easy to use ant task to make MacOS application bundles.

