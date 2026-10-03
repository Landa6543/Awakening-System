import asyncio, edge_tts, av, os

async def generate_test():
    text = "領域展開... 伏魔御廚子。" # Ryōiki Tenkai: Fukuma Mizushi
    voice = "ja-JP-KeitaNeural"
    communicate = edge_tts.Communicate(text, voice, pitch="-10Hz", rate="-5%")
    await communicate.save("test_sukuna.mp3")
    print("Saved test_sukuna.mp3")

    # Convert to Vorbis ogg with PyAV
    inp = av.open("test_sukuna.mp3")
    out = av.open("test_sukuna.ogg", 'w', format='ogg')
    out_stream = out.add_stream('vorbis', rate=48000)
    out_stream.layout = 'stereo'
    out_stream.format = 'fltp'

    resampler = av.AudioResampler(format='fltp', layout='stereo', rate=48000)
    for frame in inp.decode(audio=0):
        frame.pts = None
        for resampled_frame in resampler.resample(frame):
            for packet in out_stream.encode(resampled_frame):
                out.mux(packet)
    for packet in out_stream.encode(None):
        out.mux(packet)
    inp.close()
    out.close()
    print("Converted to test_sukuna.ogg, size:", os.path.getsize("test_sukuna.ogg"))

asyncio.run(generate_test())
