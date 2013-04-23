#!/bin/zsh

# sc - Advanced soundcloud music downloading tool from http://thirdletter.com
# Cade Scroggins (scroggins.cade@gmail.com)

errorFunction() {
	RESPONSES="invalid:bad:baseless:fallacious:false:ill-founded:illogical:inoperative:irrational:mad:notbinding:notworking:nugatory:null:nullandvoid:reasonless:sophistic:unreasonable:unreasoned:unscientific:unsound:untrue:void:wrong:absurdity:badjob:blunder:boner:boo-boo:delinquency:delusion:deviation:erratum:failure:fall:fallacy:falsehood:falsity:fault:fauxpas:flaw:glitch:goof:howler:inaccuracy:lapse:misapprehension:misbelief:miscalculation:misconception:miscue:misdeed:misjudgment:mismanagement:miss:misstep:misunderstanding:offense:omission:oversight:screamer:screw-up:sin:slight:slip:slipup:solecism:stumble:transgression:trespass:untruth:wrongdoing"
	RESPONSE=$(echo "$RESPONSES" | cut -d: -f$(($RANDOM % 72 + 1)))
	
	echo "$RESPONSE -► $SCRIPT_NAME -h"
	exit 1; }

usageFunction() {
	printf "USAGE: $SCRIPT_NAME [options] <http://soundcloud.com/...>

OPTIONS:
    -h  Print this message! Hoorah.

    -L  Login to soundcloud. A cookie is stored at
        ~/.scookie. To logout, remove the cookie, or
        login again with bad login information.

    -w  See who is logged in.

    -o <path>
        Place all downloaded files in this directory.
        If the specified directory does not exist,
        then it will be created. By default, songs
        will be downloaded to the working directory.

    -A <string>
    	Specify a custom album name for downloaded
    	songs. Default is \"Soundcloud\".

    -m <regex>
        (Match) Only download a song when its title
        contains a specified string.

    -a <regex>
        (Avoid) Only download a song when its title
        does not contain a specified string.

    -T <time>
        Only download songs that are longer than the
        specified time in the following formats:
        HH:MM:SS

    -t <time>
        Only download songs that are shorter than the
        specified time in the following formats:
        HH:MM:SS

    -n <number>
        Download or list a specific number of songs or
        links.

    -p <number>
        Download or list a specific number of pages of
        songs or links.

    -l  Only print the streaming links to standard
        output and don't download anyting.

    -v  Enable verbose mode.\n"
	exit 0; }

whoIsLogged() {
	if test -f ~/.scookie; then
		cat ~/.scookie | grep "sure+you+had+the+right+username" > /dev/null && printf "You are not logged in.\n" \
	                                                                        || printf "Logged in as $(cat ~/.scookie | sed -n 12p | awk '{print $7}' | tr "+" " ").\n"
	else
		printf "You are not logged in.\n"
	fi
	
	exit 0
}

loginFunction() {
	rm -f ~/.scookie
	
	printf "Email: "; read EMAIL
	printf "Password: "; read -s PASS
	echo
	
	curl -sA "Mozilla/4.73 [en] (X11; U; Linux 2.2.15 i686)" \
	--cookie ~/.scookie --cookie-jar ~/.scookie \
	--data "login_submitted_via=static" \
	--data-urlencode "username=$EMAIL" \
	--data-urlencode "password=$PASS" \
	--location "https://soundcloud.com/session" > /dev/null
	
	echo
	whoIsLogged
}

headsUpFunction() {
	echo "Loading: $ORIGINAL_PAGE... (Press ^c to quit)"; }

HTMLTitleConvertFunction() {
	echo "$1" | sed 's|amp\;||g;s|\&quot\;|\"|g;s|\&lt\;|<|g;s|\&rt\;|>|g;s|^\.||' | tr '#' '+' | tr '/' ':'; }

HTMLCommentsConvertFunction() {
	echo "$1" | sed 's|amp\;||g;s|\&quot\;|\"|g;s|\&lt\;|<|g;s|\&rt\;|>|g'; }
	
convertToSecondsFunction() {
	if [ "$(echo "$1" | grep -o "^[0-9][0-9]:[0-9][0-9]:[0-9][0-9]$")" ]; then
		echo $1 | awk -F: '{ print ($1 * 3600) + ($2 * 60) + $3 }'
	fi; }
	
isIntegerFunction() {
    S=$(echo $1 | tr -d 0-9)
    [ -z "$S" ] && return 1 \
                || return 0 }
	
SCRIPT_NAME="$(basename $0)"
ORIGINAL_PAGE="$(echo "${(P)#}" | sed 's|https:|http:|')"
BASE_URL="$(echo "$ORIGINAL_PAGE" | grep -Eo '^https*://w*\.*.*\.(net|com|org|cc|gov|edu|int|mil|br|ca|cn|fr|in|jp|ru)')"
HAS_EYED3="$([ "$(which eyeD3)" = "eyeD3 not found" ] && printf "FALSE" || printf "TRUE")"
PAGES_TOTAL=1000000
SHOW_LINKS=FALSE
DESTINATION="./"
ALBUM="Soundcloud"
VERBOSE=FALSE
NO_NEWLINE_NEEDED=TRUE
TOTAL=0
AMOUNT_TOTAL=1000000

while getopts "hLwo:A:m:a:T:t:n:p:lv" OPTIONS; do
    case "$OPTIONS" in
		h) usageFunction ;;
		L) loginFunction ;;
		w) whoIsLogged ;;
		o) DESTINATION="$OPTARG" ;;
		A) ALBUM="$OPTARG" ;;
		m) MATCH_EXPRESSION="$OPTARG" ;;
		a) AVOID_EXPRESSION="$OPTARG" ;;
		T) LONGER_THAN="$OPTARG" ;;
		t) SHORTER_THAN="$OPTARG" ;;
		n) AMOUNT_TOTAL="$OPTARG" ;;
		p) PAGES_TOTAL="$OPTARG" ;;
		l) SHOW_LINKS=TRUE ;;
		v) VERBOSE=TRUE ;;
        ?) echo; errorFunction;;
    esac
done

case "$BASE_URL" in
	http://soundcloud.com)
		DESTINATION="$(dirname $DESTINATION/NOSLASH)/"

		if [ -n "$LONGER_THAN" ]; then
			LONGER_THAN_CONVERTED="$(convertToSecondsFunction "$LONGER_THAN")"
			[ -z $LONGER_THAN_CONVERTED ] && errorFunction
		fi
		if [ -n "$SHORTER_THAN" ]; then
			SHORTER_THAN_CONVERTED="$(convertToSecondsFunction "$SHORTER_THAN")"
			[ -z $SHORTER_THAN_CONVERTED ] && errorFunction
		fi
		
		if isIntegerFunction "$AMOUNT_TOTAL"; then
			errorFunction
		fi
		if isIntegerFunction "$PAGES_TOTAL"; then
			errorFunction
		fi
			
		if [ $SHOW_LINKS = FALSE ]; then
			[ $HAS_EYED3 = FALSE ] && echo "Warning: $SCRIPT_NAME does not embed id3 tags without eyeD3 installed: http://eyed3.nicfit.net/"
			headsUpFunction
		fi
	
		SOURCE="$(curl -A "Mozilla/5.0" -s --cookie ~/.scookie "$ORIGINAL_PAGE")"
		PAGES=$(echo "$SOURCE" | grep -Eo "(current\">|page=)[[:digit:]]*" | cut -d\= -f2 | cut -d\> -f2 | sort -rn | head -1)
		[ -z "$PAGES" ] && PAGES=1
		
		if [ $(echo "$ORIGINAL_PAGE" | grep 'page=[[:digit:]]*') ]; then
			FIRST_PAGE_NUM=$(echo "$ORIGINAL_PAGE" | grep -o "page=[[:digit:]]*" | cut -d\= -f2)
			PAGE_NUM=$FIRST_PAGE_NUM
			ORIGINAL_PAGE="$(echo "$ORIGINAL_PAGE" | sed "s|page=[[:digit:]]*&*||")"
		else
			PAGE_NUM=1
			FIRST_PAGE_NUM=1
		fi
		
		SINGLE_SONG="$(echo "$SOURCE" | grep "info-header large" | grep -v "class=\"set")"
		SET="$(echo "$ORIGINAL_PAGE" | grep -E "/sets/.+")"

		[ $SINGLE_SONG ] || [ $PAGES_TOTAL = 1 ] && PAGES=$PAGE_NUM

		for (( $PAGE_NUM; PAGE_NUM <= $PAGES; PAGE_NUM++ )); do
			[ $PAGE_NUM -gt $PAGES_TOTAL ] && exit 0

			if [ $SHOW_LINKS = FALSE ]; then
				[ $NO_NEWLINE_NEEDED = FALSE ] && echo
				echo "Scanning Page... $PAGE_NUM/$PAGES"
				NO_NEWLINE_NEEDED=TRUE
			fi

			if [ "$PAGE_NUM" -gt "$FIRST_PAGE_NUM" ]; then
				if [ "$(echo "$ORIGINAL_PAGE" | grep "/search\?")" ]; then
					NEXT_PAGE="$(echo "$ORIGINAL_PAGE" | sed "s|/search\?|/search?page=$PAGE_NUM\&|")"
					SOURCE="$(curl -A "Mozilla/5.0" -s --cookie ~/.scookie "$NEXT_PAGE")"
				else
					NEXT_PAGE="$(echo "$ORIGINAL_PAGE?page=$PAGE_NUM")"
					SOURCE="$(curl -A "Mozilla/5.0" -s --cookie ~/.scookie "$NEXT_PAGE")"
				fi
			fi
			
			if [ ! $SET ]; then
				ARTISTS="$(HTMLTitleConvertFunction "$(echo "$SOURCE" | grep -o "user-name\">.*</a>" | sed -E 's|user-name\">(.*)</a>|\1|')")"
				SONG_TITLES="$(HTMLTitleConvertFunction "$(echo "$SOURCE" | grep -o "title\":.*\",\"commentable\"" | sed -e 's|title\":\"||;s|\",\"commentable\"||')")"
				SONG_URLS="$(echo "$SOURCE" | grep -Eo "(http://media.soundcloud.com/stream/[^\"<]*|href=\"/[^/]*/[^/]*/\?size=medium\"|/[^\"<>]*/download)" | uniq | sed 's|href=\"|http://soundcloud.com|' | sed '/.*download$/{n;d;}')"
				STREAM_URLS="$(echo "$SOURCE" | grep -o "http://media.soundcloud.com/stream/[^\"<]*")"
				ARTWORK_URLS="$(echo "$SOURCE" | grep -o "http://i1.sndcdn.com/artworks.*-crop\.jpg")"
				DURATIONS="$(echo "$SOURCE" | grep -o "duration\":[[:digit:]]*" | cut -d: -f2 | sed 's|...$||')"
				COMMENT_URLS="$(echo "$SOURCE" | grep -o "commentUri\":\"\/[^\"]*/comments/\"" | uniq | sed 's|commentUri\":\"|http://soundcloud.com|' | tr -d '"')"
				ACTUAL_SONG_PAGE_PATHS="$(echo "$SOURCE" | grep -o "uri\":\"/[^/]*/[^/\"]*\"" | cut -d\" -f3)"
			else
				SONG_URLS="$(echo "$SOURCE" | grep -oE "(uri\":\"/[^/]*/[^/\"]*\"|href=\"/[^/]*/[^/]*/\?size=large\")" | sed 's|/\?size=large||' | tr '=' '"' | cut -d\" -f3 | sed -e "s|^|http://soundcloud.com|" -e 's|$|/?size=medium|')"
			fi
			
			if [ -z "$SONG_URLS" ]; then
				printf "No songs were found at: $ORIGINAL_PAGE\n"
				exit 1
			fi

			if [ "$SINGLE_SONG" ]; then
				SONG_COUNT=1	
			else
				SONG_COUNT=$(echo "$SONG_URLS" | wc -l)
				ARTWORK_TITLES="$(HTMLTitleConvertFunction "$(echo "$SOURCE" | grep -o "[^>]*Artwork")")"
			fi

			for (( SONG_ID=1, COMMENT_ID=1, ARTWORK_ID=1, THE_REST_ID=1; SONG_ID <= $SONG_COUNT; SONG_ID+=1 )); do
				[ $TOTAL -eq $AMOUNT_TOTAL ] && exit 0
				SONG_URL="$(echo "$SONG_URLS" | sed -n "$SONG_ID"p)"
				
				if [ "$(echo "$SONG_URL" | grep "\?stream_token=")" ]; then
					if [ "$(echo "$SONG_URLS" | sed -n "$((SONG_ID + 1))"p | grep "/\?size=medium")" ]; then
						SONG_URL="$(echo "$COMMENT_URLS" | sed -n "$COMMENT_ID"p | sed "s|/comments/|/?size=medium|")"
					fi
					let COMMENT_ID+=1
				fi
				
				if [ "$(echo "$SONG_URL" | grep "/?size=medium")" ]; then
					SET_SONG_PAGE_SOURCE="$(curl -A "Mozilla/5.0" -s --cookie ~/.scookie "$SONG_URL")"
					ACTUAL_SONG_PAGE_PATH="$(echo "$SONG_URL" | sed 's|/?size=medium||;s|http://soundcloud.com||')"
					ARTIST="$(HTMLTitleConvertFunction "$(echo "$SET_SONG_PAGE_SOURCE" | grep -o "user-name\">.*</a>" | sed -E 's|user-name\">(.*)</a>|\1|' | head -1)")"
					SONG_TITLE="$(HTMLTitleConvertFunction "$(echo "$SET_SONG_PAGE_SOURCE" | grep -o "title\":.*\",\"commentable\"" | sed 's|title\":\"||;s|\",\"commentable\"||' | head -1)")"
					SONG_URL="$(echo "$SET_SONG_PAGE_SOURCE" | grep -Eo "(http://media.soundcloud.com/stream/[^\"<]*\?stream_token=.....|/[^\"]*/download)" | uniq | head -1)"
					STREAM_URL="$(echo "$SET_SONG_PAGE_SOURCE" | grep -o "http://media.soundcloud.com/stream/[^\"<]*\?stream_token=[^\"<]*" | head -1)"
					ARTWORK_TITLE="$ARTIST"
					ARTWORK_URL="$(echo "$SET_SONG_PAGE_SOURCE" | grep -o "http://i1.sndcdn.com/artworks.*-crop\.jpg")"
					DURATION="$(echo "$SET_SONG_PAGE_SOURCE" | grep -o "duration\":[[:digit:]]*" | cut -d: -f2 | head -1 | sed 's|...$||')"
				else
					ARTIST="$(echo "$ARTISTS" | sed -n "$THE_REST_ID"p)"
					SONG_TITLE="$(echo "$SONG_TITLES" | sed -n "$THE_REST_ID"p)"
					DURATION="$(echo "$DURATIONS" | sed -ne "$THE_REST_ID"p)"
					ACTUAL_SONG_PAGE_PATH="$(echo "$ACTUAL_SONG_PAGE_PATHS" | sed -ne "$THE_REST_ID"p)"
					STREAM_URL="$(echo "$STREAM_URLS" | sed -ne "$THE_REST_ID"p)"

					if [ "$SINGLE_SONG" ]; then
						ARTWORK_TITLE="$(echo "$SONG_TITLE" | sed 's/[^[:alpha:][:digit:]]//g')"
						ARTWORK_URL="$ARTWORK_URLS"
					else
						PLAIN_SONG_TITLE="$(echo "$SONG_TITLE" | sed 's/[^[:alpha:][:digit:]]//g')"
						PLAIN_ARTWORK_TITLES="$(echo "$ARTWORK_TITLES" | sed 's/[^[:alpha:][:digit:]]//g')"
						ARTWORK_TITLE="$(echo "$PLAIN_ARTWORK_TITLES" | grep -o "$PLAIN_SONG_TITLE")"
						if [ "$ARTWORK_TITLE" = "$PLAIN_SONG_TITLE" ]; then
							ARTWORK_URL="$(echo "$ARTWORK_URLS" | sed -n "$ARTWORK_ID"p)"
							let ARTWORK_ID+=1
						fi
					fi
					let THE_REST_ID+=1
				fi
				
				if [ "$(echo "$SONG_URL" | grep "/download")" ]; then
					SONG_URL="$(echo "$SONG_URL" | grep "/download" | sed 's|^|http://soundcloud.com|')"
					HQ=TRUE
				else
					HQ=FALSE
				fi
				
				H=$(( DURATION / 3600 ))
				M=$(( ( DURATION / 60 ) % 60 ))
				S=$(( DURATION % 60 ))
				READABLE_DURATION="$(printf "%02d:%02d:%02d\n" $H $M $S)"
				
				# IGNORE SONG IF ITS TITLE DOESN'T MATCH...
				if [ -n "$MATCH_EXPRESSION" ]; then
					if [ ! "$(echo $SONG_TITLE | grep -E "$MATCH_EXPRESSION")" ]; then
						if [ $VERBOSE = TRUE ] && [ $SHOW_LINKS = FALSE ]; then
							[ $NO_NEWLINE_NEEDED = FALSE ] && echo
							echo "Doesn't match: $SONG_TITLE"
							NO_NEWLINE_NEEDED=TRUE
						fi
						continue
					fi
				fi
				
				# IGNORE SONG IF ITS TITLE MATCHES...
				if [ -n "$AVOID_EXPRESSION" ]; then
					if [ "$(echo $SONG_TITLE | grep -E "$AVOID_EXPRESSION")" ]; then
						if [ $VERBOSE = TRUE ] && [ $SHOW_LINKS = FALSE ]; then
							[ $NO_NEWLINE_NEEDED = FALSE ] && echo
							echo "Avoided: $(echo "$SONG_TITLE" | grep -E --color=always "$AVOID_EXPRESSION")"
							NO_NEWLINE_NEEDED=TRUE
						fi
						continue
					fi
				fi
				
				# IGNORE SONG IF IT'S LONGER THAN...
				if [ -n "$LONGER_THAN_CONVERTED" ]; then
					if [ "$LONGER_THAN_CONVERTED" -gt "$DURATION" ]; then
						if [ $VERBOSE = TRUE ] && [ $SHOW_LINKS = FALSE ]; then
							[ $NO_NEWLINE_NEEDED = FALSE ] && echo
							echo "Too short: $SONG_TITLE"
							NO_NEWLINE_NEEDED=TRUE
						fi
						continue
					fi
				fi
				
				# IGNORE SONG IF IT'S SHORTER THAN...
				if [ -n "$SHORTER_THAN_CONVERTED" ]; then
					if [ "$SHORTER_THAN_CONVERTED" -lt "$DURATION" ]; then
						if [ $VERBOSE = TRUE ] && [ $SHOW_LINKS = FALSE ]; then
							[ $NO_NEWLINE_NEEDED = FALSE ] && echo
							echo "Too long: $SONG_TITLE"
							NO_NEWLINE_NEEDED=TRUE
						fi
						continue
					fi
				fi
				
				if [ $SHOW_LINKS = FALSE ]; then
					if [ -n "$SONG_URL" ] && [ ! -f "$DESTINATION$SONG_TITLE".mp3 ]; then
						NO_NEWLINE_NEEDED=FALSE
						
						echo -e "\n$SONG_TITLE $([ $HQ = TRUE ] && printf "(HQ)")"
						if [ $VERBOSE = TRUE ]; then
							echo "------------------------------------------------------------------------"
							echo "Page: http://soundcloud.com$ACTUAL_SONG_PAGE_PATH"
							echo "Song: $SONG_URL"
							[ -n "$ARTWORK_URL" ] && echo "Art:  $ARTWORK_URL"
							echo "Time: $READABLE_DURATION"
							COMMENTS="$(curl -s http://soundcloud.com"$ACTUAL_SONG_PAGE_PATH" | tr '\n' '|' | grep -o "class=\"time\">at|[^|]*|[^|]*|[^|]*</p></div>" | sed 's|class=\"time\">at\|||;s|</span>[^=]*=[^=]*=[^=]*=[^=]*=| |;s| [^,]*,.*>on \(...\)[^ ]*\(.*\)</abbr>.*<p>| \1\2: |;s|</p></div>||;s|<a href=\"\([^"]*\).*</a>|\1|;s|, *|, |;s|@: *||;s|\.|:|' | tr -d "'" | awk '{ if (length($0) > 28) print }' | awk '{ if (length($0) < 80) print }' | sort -n)"
							[ -n "$COMMENTS" ] && echo -e "------------------------------------------------------------------------\n$(HTMLCommentsConvertFunction $COMMENTS)\n------------------------------------------------------------------------"
						fi
						
						curl --cookie ~/.scookie --create-dirs -C - -#Lo "$DESTINATION$SONG_TITLE".part "$SONG_URL"
						
						[ $? -eq 0 ] && let TOTAL+=1
						
						if [ "$(file "$DESTINATION$SONG_TITLE".part | grep "MPEG v4")" ]; then
							EXTENSION=m4a
						else
							EXTENSION=mp3
						fi
						
						mv "$DESTINATION$SONG_TITLE".part "$DESTINATION$SONG_TITLE".$EXTENSION
						
						if [ $HAS_EYED3 = TRUE ] && [ $EXTENSION = mp3 ] && [ -f "$DESTINATION$SONG_TITLE".$EXTENSION ]; then
							if [ "$(echo "$SONG_URL" | grep "/download")" ]; then
								eyeD3 --album="$ALBUM" --genre="" "$DESTINATION$SONG_TITLE".$EXTENSION &> /dev/null
							else
								eyeD3 --artist="$ARTIST" --album="$ALBUM" --genre="" "$DESTINATION$SONG_TITLE".$EXTENSION &> /dev/null
							fi
							if [ -n "$ARTWORK_URL" ]; then
								curl --cookie ~/.scookie --create-dirs -so /tmp/"$ARTWORK_TITLE".jpg "$ARTWORK_URL"
								eyeD3 --remove-images "$DESTINATION$SONG_TITLE".$EXTENSION &> /dev/null
								eyeD3 --add-image=/tmp/"$ARTWORK_TITLE".jpg:FRONT_COVER "$DESTINATION$SONG_TITLE".$EXTENSION &> /dev/null
							fi
						fi
					else
						if [ $VERBOSE = TRUE ] && [ $SHOW_LINKS = FALSE ]; then
							[ $NO_NEWLINE_NEEDED = FALSE ] && echo
							echo "File exists: "$DESTINATION$SONG_TITLE".mp3"
							NO_NEWLINE_NEEDED=TRUE
						fi
					fi
				else
					echo "$STREAM_URL#$ACTUAL_SONG_PAGE_PATH"
					[ $? -eq 0 ] && let TOTAL+=1
				fi
				unset ARTWORK_TITLE ARTWORK_URL
			done
		done
	;;
	"")
		errorFunction
	;;
	*)
		echo -e "$BASE_URL is not supported!\n"
		errorFunction
	;;
esac
