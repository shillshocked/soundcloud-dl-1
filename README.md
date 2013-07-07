This branched version detects the proper filetype if not mp3 and still allows for limited mp3 tagging with non-mp3 files. It also converts .wav and .aif to .flac to save space.

The only thing to fix is to add full tag support for the other formats. eye3D only supports mp3 in theory, so something like python-mutagen or libtag may be necessary.


This is a zsh script that downloads http://soundcloud.com music from links that are provided by the user. There are a few options that can narrow down what you want to download, or even just print out the direct links.

Setup
-----

Put the script somewhere and make sure it is added to your `$PATH`. Don't forget to make it executable as well.

		mkdir -p /usr/local/bin && chmod +x /path/to/script && mv !$ /usr/local/bin && sc -h


Examples
--------
 Login to Soundcloud

        sc -L

 Download all of your favorite songs (once you are logged in)

        sc http://soundcloud.com/you/favorites

 Download the blink-182 EP - Dogs Eating Dogs, add the appropriate album title (-A "Dogs Eating Dogs - EP"), and place the songs on your desktop (-o ~/Desktop).
 
        sc -o ~/Desktop -A "Dogs Eating Dogs - EP" https://soundcloud.com/blink-182/sets/dogs-eating-dogs-ep

 List the links (-l) of Addergebroed songs that are longer than three minutes (-T 00:03:00), but shorter than five minutes and thirty seconds (-t 00:05:30).
        
        sc -lT 00:03:00 -t 00:05:30 http://soundcloud.com/addergebroed

 Download songs on the first page (-p 1) of the dubstep library that are less than ten minutes long (-t 00:10:00) and aren't mixes (-a "[Mm][Ii][Xx]"). Print out extra information about the downloads (-v)
        
        sc -vp 1 -t 00:10:00 -a "[Mm][Ii][Xx]" http://soundcloud.com/dubstep

Notes
-----
Based on previous efforts of the script at http://360percents.com and cade scroggin's modification.
