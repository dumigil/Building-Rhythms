#%%
import pandas as pd
import numpy as np
from scipy import stats
import urllib.request
import json 
from sklearn.neighbors import KNeighborsClassifier

#%%
def predict_func(test_json_string):
    url_path = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Room_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson"


    with urllib.request.urlopen(url_path) as url:
        data = json.loads(url.read().decode())

    df_mega = pd.DataFrame(columns = ['MAC','RSSI','BSSID','Room_ID','ObjectId','Time_Stamp'])
    
    for attr in data['features']:
        temp = (attr['attributes'])
        df_mega=df_mega.append( temp, ignore_index=True)
    
    a = df_mega.MAC.unique()
    df_mega['macno'] = df_mega.MAC.apply(lambda x: np.where(a==x)[0][0])

    print(f"Max nof unique identifier is: {max(df_mega.macno)}")

    df_mega=df_mega.astype({'RSSI': 'float32'}, copy=False)
    # ////////////////// DF MEGA is the df of Araduianio Table XD ////////////////////

    def indexer(a,x):
        x=x.lower()
        ind = np.where(a==x)[0]
        if len(ind)!=0:
            return ind[0]

# {\"attributes\":{"MAC":"02:15:b2:00:01:00","RSSI":-50}}, {"attributes":{"MAC":"02:15:b2:00:01:00","RSSI":-50}}, {"attributes":{"MAC":"02:15:b2:00:01:00","RSSI":-50}}

    #to load the test dataset as a pandas df
    test_df = pd.read_json(test_json_string)
    test_df.loc[:,"MAC"] = test_df.attributes.apply(lambda x: x["MAC"])
    test_df.loc[:,"signal"] = test_df.attributes.apply(lambda x: float(x["RSSI"]))
    test_df.drop('attributes', axis=1,  inplace=True)

    # training data /////////////////////////

    x_train = df_mega[['macno' , 'RSSI']]
    y_train = pd.Series(df_mega.Room_ID)
    
    x_test = test_df[['signal' , 'MAC']].copy()
    
    x_test['macno'] = x_test.MAC.apply(lambda x: indexer(a,x))
    x_test.drop(['MAC'], axis=1, inplace=True)
    x_test.reset_index(inplace=True, drop=True)
    x_test.dropna(inplace=True)
    x_test = x_test[['macno','signal']]
    
    # train kNN model with training data ////////////////
    knn = KNeighborsClassifier(n_neighbors = 7, weights='distance', algorithm='auto')
    knn.fit(x_train , y_train)

    # predicting using trained model
    pred = knn.predict(x_test)
    room =str(stats.mode(pred)[0][0])

    return(room)


