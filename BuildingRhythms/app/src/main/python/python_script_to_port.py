#%%
from joblib.parallel import DEFAULT_MP_CONTEXT
import pandas as pd
import numpy as np
from scipy import stats
import urllib3
import json 
from sklearn.neighbors import KNeighborsClassifier

#%%
def predict_func(test_json_string, time=0):
    url_path = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Room_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson"
    #url_path = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/arcgis/rest/services/Arduino_Table_ROOM/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Table_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson"
    
    http = urllib3.PoolManager()
    r = http.request('GET', url_path)
    data = json.loads(r.data.decode('utf-8'))

    df_mega = pd.DataFrame.from_dict(data['features'])
    df_mega.loc[:, "MAC"] = df_mega.attributes.apply(lambda x: x["MAC"])
    df_mega.loc[:, "RSSI"] = df_mega.attributes.apply(lambda x: float(x["RSSI"]))
    #df_mega.loc[:, "Room_ID"] = df_mega.attributes.apply(lambda x: x["Table_ID"])
    df_mega.loc[:, "Room_ID"] = df_mega.attributes.apply(lambda x: x["Room_ID"])
    df_mega.drop('attributes', axis=1, inplace=True)

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
    # y_train = pd.Series(df_mega.Room_ID)
    
    x_test = test_df[['signal' , 'MAC']].copy()
    
    x_test['macno'] = x_test.MAC.apply(lambda x: indexer(a,x))
    x_test.drop(['MAC'], axis=1, inplace=True)
    x_test.reset_index(inplace=True, drop=True)
    x_test.dropna(inplace=True)
    x_test = x_test[['macno','signal']]
    
    try:
            
        # train kNN model with training data ////////////////
        knn = KNeighborsClassifier(n_neighbors = 7, weights='distance', algorithm='auto')
        knn.fit(x_train , y_train)

        # predicting using trained model
        pred = knn.predict(x_test)
        room =str(stats.mode(pred)[0][0])

        return(room)
    except:
        return "Couldn't locate the room you are in :("

