import json
import csv


features = []
c = 0
with open ("dummycsv.csv", newline='') as csvfile:
    line = csv.reader(csvfile, delimiter=',', quotechar='|')
    for l in line:
        x = {
            "Time_Stamp": l[1],
            "MAC": l[2],
            "BSSID": l[3],
            "RSSI":l[4],
            "Room_ID": l[5],
        }
        y = {
            "attributes": x
        }
        featureString = json.dumps(y, indent=4, sort_keys=True)
        print(featureString)
        print(",")
        features.append(y)
#print(features)

        

