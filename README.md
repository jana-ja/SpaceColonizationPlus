# Space Colonization Plus - Bachelor Thesis Project

Using a Space Colonizatio Algorithm, space can be defined through a point cloud and 3D models can be "grown" by colonizing that space. This technique is well suited for models of trees, because it matches the growing process of real life trees.

For my [Bachelors Thesis](https://github.com/user-attachments/files/20094078/Bachelor_Jansen.pdf) I implemented a Space Colonization algorithm based on a <a href="http://algorithmicbotany.org/papers/runionsa.th2008.pdf">paper by Adam Runions</a> and added the recognition of environmental factors like sunlight and shadows being cast by nearby objects.

<p align="center">
  <img src="https://github.com/user-attachments/assets/36e5e093-81a7-4332-8a8a-8d4e0118ccb8" alt="Circuit" width="24%"/>
  <img src="https://github.com/user-attachments/assets/f963d072-0ca8-4cb2-92a3-23b5834c13a18" alt="Circuit" width="24%"/>
  <img src="https://github.com/user-attachments/assets/aca10eb9-266f-43cd-8afd-855f401db1e1" alt="Circuit" width="24%"/>
  <img src="https://github.com/user-attachments/assets/9e8d49c9-f132-46e9-a331-fa0c3edd38ee" alt="Circuit" width="24%"/>  
</p>

The pointcloud can be defined through arbitrary curves, which are then rotated around a central axis. This pointcloud determines the general shape and height of a tree. Additional parameters allow finer control over the growing process and the density of branches and smaller twigs within the tree.

<p align="center">
  <img src="https://github.com/user-attachments/assets/47ecf6d9-fb65-4704-9fcf-c717b12bb48c" alt="Circuit" width="24%"/>
  <img src="https://github.com/user-attachments/assets/e61580d4-d208-4379-853c-faae42485059" alt="Circuit" width="24%"/>
</p>

In addition to the underlying algorithm, here the incoming sunlight is taken into account when simulation the tree growth. The incoming sunlight is accumulated over a day with the path of the sun being calulated dependend on the local coordinates and time of the year. When there is no obstacle in the way to block the sunlight, that results in a tree that is tilted towards the south (on northern hemisphere). The degree of influence the light has on tree growth can be set through a parameter.

<p align="center">
  <img src="https://github.com/user-attachments/assets/58038416-f954-4020-887c-3bb2c602451b8" alt="Circuit" width="24%"/>
  <img src="https://github.com/user-attachments/assets/abf41ec5-a930-430f-a12b-18fbe77cb345" alt="Circuit" width="24%"/>
</p>

Implemented in Java3D
