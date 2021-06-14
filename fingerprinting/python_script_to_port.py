import pandas as pd
import numpy as np
from scipy import stats
from sklearn.neighbors import KNeighborsClassifier
import  urllib.request, json 


url_path = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Room_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson"


with urllib.request.urlopen(url_path) as url:
    data = json.loads(url.read().decode())

df_mega = pd.DataFrame(columns = ['MAC','RSSI','BSSID','Room_ID','ObjectId','Time_Stamp'])
# s = time.time()
for attr in data['features']:
    temp = (attr['attributes'])
    df_mega=df_mega.append( temp, ignore_index=True)
# print(f"It took us {time.time()-s} sec")

a = df_mega.MAC.unique()
df_mega['macno'] = df_mega.MAC.apply(lambda x: np.where(a==x)[0][0])

print(f"Max nof unique identifier is: {max(df_mega.macno)}")

df_mega=df_mega.astype({'RSSI': 'float32'}, copy=False)


def indexer(a,x):
    x=x.lower()
    ind = np.where(a==x)[0]
    if len(ind)!=0:
        return ind[0]

# {\"attributes\":{"MAC":"02:15:b2:00:01:00","RSSI":-50}}, {"attributes":{"MAC":"02:15:b2:00:01:00","RSSI":-50}}, {"attributes":{"MAC":"02:15:b2:00:01:00","RSSI":-50}}
#%%
def jsonToDF(test_json_string): 
    """
    test_json_string : json string from output, it has the list brackets around it as well
    returns a test df
    """

    #to load the test dataset as a pandas df
    # j = json.loads(test_json_string)
    # test_df = pd.DataFrame(columns = ['MAC','RSSI','BSSID','Room_ID','ObjectId','Time_Stamp'])
    test_df = pd.read_json(test_json_string)
    test_df.loc[:,"MAC"] = test_df.attributes.apply(lambda x: x["MAC"])
    test_df.loc[:,"signal"] = test_df.attributes.apply(lambda x: float(x["RSSI"]))
    test_df.drop('attributes', axis=1,  inplace=True)
    return test_df


#%%

def predictor(test_json_string, df_mega):
    test_df = jsonToDF(test_json_string)
    # file = 'Scan_30s_01_560.rtf'
    # test_df = d[file]

    # training data /////////////////////////

    x_train = df_mega[['macno' , 'RSSI']]
    y_train = pd.Series(df_mega.Room_ID)
    # print(x_train.head())

    x_test = test_df[['signal' , 'MAC']].copy()
    # y_test = pd.Series(test_df.place)
    x_test['macno'] = x_test.MAC.apply(lambda x: indexer(a,x))
    x_test.drop(['MAC'], axis=1, inplace=True)
    x_test.reset_index(inplace=True, drop=True)
    x_test.dropna(inplace=True)
    x_test = x_test[['macno','signal']]
    # train kNN model with training data ////////////////
    knn = KNeighborsClassifier(n_neighbors = 7, weights='distance', algorithm='auto')
    knn.fit(x_train , y_train)

    # saving the trained model  
    # f_name = './_data/training_data/knn_model.pkl'
    # pickle.dump(knn, open(f_name, 'wb'))
    # predicting using trained model
    pred = knn.predict(x_test)
    room =str(stats.mode(pred)[0][0])

    return(room)


