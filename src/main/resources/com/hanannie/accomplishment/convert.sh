for i in *.mp3; do ffmpeg -i "$i" -acodec pcm_u8 -ar 22050 "${i%.*}.wav" done

