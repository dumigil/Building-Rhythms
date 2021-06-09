with open("index.txt", 'w') as fh:
    for e in range(15000):
        fh.write(str(e)+ "," )
