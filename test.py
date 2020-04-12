import matplotlib.pyplot as plt
import csv
import pandas as pd
import math
from mpl_toolkits import mplot3d

file = 'statisch'
### alle einlesen
data = pd.read_csv('eval_' + file + '.txt', index_col=False, usecols = [2,3,4,5,6,7,8,9,10,11,12,13,14])

#data['azimut'] = data['azimut'].map(lambda x : x-360 if x >= 315 else x )
#data['avgAzimut'] = data['avgAzimut'].map(lambda x : x-360 if x >= 315 else x )

factor2 =  data['factor']
azimut2 = data['azimut']
elev2 = data['elev']
areaInLight2 = data['areaInLight']
avgAzimut2 = data['avgAzimut']
#print(data['nodes'])
### pro faktor gruppieren (durchschnitte)
data = data.groupby(['factor'], as_index=False).mean()

###
print(data)
#################alle punkte und dann mean kurve
###
factor = data['factor']
azimut = data['azimut']
elev = data['elev']
nodes = data['nodes']
branches = data['branches']
avgX = data['avgNodeX']
avgY = data['avgNodeY']
avgZ = data['avgNodeZ']
avgAzimut = data['avgAzimut']
avgRadius = data['avgRadius']
nodesInLight = data['nodesInLight']
areaInLight = data['areaInLight']
height = data['height']

path = 'E:\\Users\\JanaJ\\Dokumente\\owncloud\\Uni\\BA\\Abschlussarbeit_Vorlage_LS7\\bilder\\kap5'


print(azimut)
print(avgAzimut)


####### plotten
####azimuth und elevation plot####
#plt.figure('Winkel')
#azimut = azimut.apply(lambda x : x-360 if x >= 315 else x )
#azimut2 = azimut2.apply(lambda x : x-360 if x >= 315 else x )


fig, ax1 = plt.subplots()

color = 'b'#'tab:blue'
ax1.set_xlabel('Faktor o')
ax1.set_ylabel('Azimutwinkel', color=color)
l1 = ax1.plot(factor, azimut, color = color, label = 'Azimut Mittelwert')
l2 = ax1.plot(factor2, azimut2, 'bo', color = color, label = 'Azimut Daten')
ax1.tick_params(axis='y', labelcolor=color)
#ax1.set_yticks([-45,0,45,90,135,180,225,270,315])
ax1.set_yticks([0,45,90,135,180,225,270,315,360])
ax1.grid()

ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis

color = 'r'#'tab:red'
ax2.set_ylabel('Höhenwinkel', color=color)  # we already handled the x-label with ax1
l3 = ax2.plot(factor, elev, color = color, label = 'Höhenwinkel')
#l4 = ax2.plot(factor2, elev2, 'ro', color = color, label = 'Azimut Daten')
ax2.tick_params(axis='y', labelcolor=color)

ax1.legend(l2+l1+l3, ['Azimut','Azimut Mittel','Höhe Mittel'])#,loc="upper left",bbox_to_anchor=(0.12, 1))
fig.tight_layout()  # otherwise the right y-label is slightly clipped
fig.savefig(path + '\\roh_winkel_' + file + '.pdf')

##plt.figure()
##azimut = azimut.apply(math.radians)
##plt.polar(azimut, factor)



####avgNode ??####
##fig, ax = plt.subplots()
##ax.scatter(avgX,avgZ)
##for i, fac in enumerate(factor):
##    ax.annotate(fac,(avgX[i],avgZ[i]))
##fig.savefig(path + '\\roh_schwerp_' + file + '_1.pdf')

#oder
    
##fig = plt.figure()
##ax = fig.add_subplot(111, projection='polar')
##avgAzimut = avgAzimut.apply(math.radians)
##ax.scatter(avgAzimut, avgRadius)
##for i, fac in enumerate(factor):
##    ax.annotate(fac,(avgAzimut[i],avgRadius[i]))
##fig.savefig(path + '\\roh_schwerp_' + file + '_2.pdf')

#oder

fig, ax1 = plt.subplots()
color = 'b'#'tab:blue'
ax1.set_xlabel('Faktor o')
ax1.set_ylabel('Schwerpunkt Azimutwinkel', color=color)
l1 = ax1.plot(factor, avgAzimut, color = color, label = 'Azimutwinkel Mittelwert')
l2 = ax1.plot(factor2, avgAzimut2, 'bo', color = color, label = 'Azimutwinkel')
ax1.tick_params(axis='y', labelcolor=color)
#ax1.set_yticks([-45,0,45,90,135,180,225,270,315])
ax1.set_yticks([0,45,90,135,180,225,270,315,360])
ax1.grid()
ax2 = ax1.twinx()  # instantiate a second axes that shares the same x-axis
color = 'r'#'tab:red'
ax2.set_ylabel('Radius', color=color)  # we already handled the x-label with ax1
l3 = ax2.plot(factor, avgRadius, color = color, label = 'Radius')
ax2.tick_params(axis='y', labelcolor=color)
ax1.legend(l2+l1+l3, ['Azimut','Azimut Mittel','Radius Mittel'], loc="upper left",bbox_to_anchor=(0.12, 1))
fig.tight_layout()  # otherwise the right y-label is slightly clipped
fig.savefig(path + '\\roh_schwerp_' + file + '_3.pdf')


####nodes und branches plot####
##plt.figure('Anzahl')
##plt.plot(factor, nodes, label = 'Anzahl Knoten')
##plt.plot(factor, branches, label = 'Anzahl Äste')
##plt.xlabel("Faktor o")
##plt.ylabel("Anzahl der Knoten, Äste")
##plt.savefig(path + '\\anzahl_' + file + '_roh.pdf')


####nodes in light plot####
plt.figure(3)
plt.plot(factor,nodesInLight)
plt.grid()
plt.xlabel("Faktor o")
plt.ylabel("Knoten des Baumes im Licht in %")
plt.savefig(path + '\\roh_nodes_light_' + file + '.pdf')


####area in light plot####
fig, ax1 = plt.subplots()
color = 'b'#'tab:blue'
ax1.plot(factor,areaInLight, color = color, label ='area light trend')
ax1.plot(factor2, areaInLight2, 'bo', color = color, label = 'Area Daten')
ax1.grid()
ax1.set_xlabel("Faktor o")
ax1.set_ylabel("Oberfläche im Licht in %")
fig.savefig(path + '\\roh_area_light_' + file + '.pdf')


####height plot####
plt.figure(5)
color = 'b'#'tab:blue'
plt.plot(factor,height, color=color)
plt.grid()
plt.xlabel("Faktor o")
plt.ylabel("Größe des Baumes in m")
plt.savefig(path + '\\roh_height_' + file + '.pdf')


plt.show()

