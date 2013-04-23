This is a zsh script that downloads soundcloud links that are provided by the user. There are a few options that can narrow down what you want to download, or even just print out the direct links.

Setup
-----

sc is a shell script, so put it somewhere and make sure it is added to your `$PATH`. 

If you're on OS X, sc is on homebrew:
        
        brew install sc

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
This script was inspired by the one at http://360percents.com. If you have any questions or comments, email me: scroggins.cade@gmail.com.
