/*=========================================================================

  Program:   Visualization Toolkit
  Module:    SpecularSpheres.cxx

  Copyright (c) Ken Martin, Will Schroeder, Bill Lorensen
  All rights reserved.
  See Copyright.txt or http://www.kitware.com/Copyright.htm for details.

     This software is distributed WITHOUT ANY WARRANTY; without even
     the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
     PURPOSE.  See the above copyright notice for more information.

=========================================================================*/
//
// This examples demonstrates the effect of specular lighting.
//

#include <OpenGL/gl.h>
#include <OpenGL/glu.h>
#include "vtkSmartPointer.h"
#include "vtkSphereSource.h"
#include "vtkPolyDataMapper.h"
#include "vtkActor.h"
#include "vtkInteractorStyle.h"
#include <vtkInteractorStyleTrackballCamera.h>
#include "vtkObjectFactory.h"
#include "vtkRenderer.h"
#include "vtkRenderWindow.h"
#include "vtkRenderWindowInteractor.h"
#include "vtkProperty.h"
#include "vtkCamera.h"
#include "vtkLight.h"
#include "vtkOpenGLPolyDataMapper.h"
#include "vtkJPEGReader.h"
#include "vtkImageData.h"
#include <vtkDataSetReader.h>
#include <vtkRectilinearGrid.h>
#include <vtkPolyData.h>
#include <vtkPointData.h>
#include <vtkPolyDataReader.h>
#include <vtkPoints.h>
#include <vtkUnsignedCharArray.h>
#include <vtkFloatArray.h>
#include <vtkDoubleArray.h>
#include <vtkCellArray.h>
#include <vtkPNGWriter.h>
#include <math.h>
#include <string>
#include <sstream>

// Basic VTK data structures copied from code provided by my professor

vtkImageData *
NewImage(int width, int height)
{
    vtkImageData *img = vtkImageData::New();
    img->SetDimensions(width, height, 1);
    img->AllocateScalars(VTK_UNSIGNED_CHAR, 3);
    
    return img;
}

void
WriteImage(vtkImageData *img, const char *filename)
{
    std::string full_filename = filename;
    full_filename += ".png";
    vtkPNGWriter *writer = vtkPNGWriter::New();
    writer->SetInputData(img);
    writer->SetFileName(full_filename.c_str());
    writer->Write();
    writer->Delete();
}

// The skeleton of this function was defined by my professor, but the actual computations were defined by me (that is, everything in the ApplyTransferFunction function)
struct TransferFunction
{
    double          min;
    double          max;
    int             numBins;
    unsigned char  *colors;  // size is 3*numBins
    double         *opacities; // size is numBins
    
    // Take in a value and applies the transfer function.
    // Step #1: figure out which bin "value" lies in.
    // If "min" is 2 and "max" is 4, and there are 10 bins, then
    //   bin 0 = 2->2.2
    //   bin 1 = 2.2->2.4
    //   bin 2 = 2.4->2.6
    //   bin 3 = 2.6->2.8
    //   bin 4 = 2.8->3.0
    //   bin 5 = 3.0->3.2
    //   bin 6 = 3.2->3.4
    //   bin 7 = 3.4->3.6
    //   bin 8 = 3.6->3.8
    //   bin 9 = 3.8->4.0
    // and, for example, a "value" of 3.15 would return the color in bin 5
    // and the opacity at "opacities[5]".
    
    void ApplyTransferFunction(double value, unsigned char *RGB, double &opacity, int frame)
    {
        int range = max-min;
        double increment = ((double) range)/numBins;
        int bin = 0;
        
        if (value < min){
            RGB[0] = 0;
            RGB[1] = 0;
            RGB[2] = 0;
            opacity = 0;
        }
        
        else if (value > max){
            RGB[0] = 255;
            RGB[1] = 255;
            RGB[2] = 255;
            opacity = 0;
        }
        
        else {
            for (float j = min; j < max; j+=increment){
                if (value <= j){
                    break;
                }
                ++bin;
            }
            
         /*   RGB[0] = colors[5*(bin%2)];
            RGB[1] = colors[4*(bin)];
            RGB[2] = colors[3*(bin%3)+1]; */
            
           /* if (frame < 500){
                int red = 192;
                int green = 192;
                int blue = 192;
                float redIncrement = ((float) (192 - colors[3*bin+1]))/500;
                float greenIncrement = ((float) (192 - colors[3*bin+2]))/500;
                float blueIncrement = ((float) (192 - colors[3*bin]))/500;
                red -= (int) (redIncrement*frame);
                green -= (int) (greenIncrement*frame);
                blue -= (int) (blueIncrement*frame);
                RGB[0] = (unsigned char) red;
                RGB[1] = (unsigned char) green;
                RGB[2] = (unsigned char) blue;
            }
            else if (frame < 1000){
                int red = colors[3*bin+1];
                int green = colors[3*bin+2];
                int blue = colors[3*bin];
                float redIncrement = ((float) (colors[3*bin+1]-colors[3*bin+1]))/500;
                float greenIncrement = ((float) (colors[3*bin+2]-colors[3*bin+1]))/500;
                float blueIncrement = ((float) (colors[3*bin])-colors[3*bin])/500;
                red -= (int) (redIncrement*(frame-500));
                green -= (int) (greenIncrement*(frame-500));
                blue -= (int) (blueIncrement*(frame-500));
                RGB[0] = (unsigned char) red;
                RGB[1] = (unsigned char) green;
                RGB[2] = (unsigned char) blue;
            }
            
            else {
                RGB[0] = 255;
                RGB[1] = 255;
                RGB[2] = 255;
            } */
            
          /*  if (frame < 400){
                int red = colors[3*bin+2];
                int green = colors[3*bin];
                int blue = colors[3*bin+1];
                float redIncrement = ((float) (colors[3*bin+2] - colors[3*bin+1]))/400;
                float greenIncrement = ((float) (colors[3*bin] - colors[3*bin+2]))/400;
                float blueIncrement = ((float) (colors[3*bin+1] - colors[3*bin]))/400;
                red -= (int) (redIncrement*frame);
                green -= (int) (greenIncrement*frame);
                blue -= (int) (blueIncrement*frame);
                RGB[0] = (unsigned char) red;
                RGB[1] = (unsigned char) green;
                RGB[2] = (unsigned char) blue;
            }
            else if (frame < 800){
                int red = colors[3*bin+1];
                int green = colors[3*bin+2];
                int blue = colors[3*bin];
                float redIncrement = ((float) (colors[3*bin+1]-colors[5*(bin%2)]))/400;
                float greenIncrement = ((float) (colors[3*bin+2]-colors[4*(bin)]))/400;
                float blueIncrement = ((float) (colors[3*bin])-colors[3*(bin%3)+1])/400;
                red -= (int) (redIncrement*(frame-400));
                green -= (int) (greenIncrement*(frame-400));
                blue -= (int) (blueIncrement*(frame-400));
                RGB[0] = (unsigned char) red;
                RGB[1] = (unsigned char) green;
                RGB[2] = (unsigned char) blue;
            }

            else {
                RGB[0] = 255;
                RGB[1] = 255;
                RGB[2] = 255;
            } */
            
            RGB[0] = colors[3*bin+2];
            RGB[1] = colors[3*bin];
            RGB[2] = colors[3*bin+1];
            
          /*  RGB[0] = colors[3*bin+1];
            RGB[1] = colors[3*bin+2];
            RGB[2] = colors[3*bin]; */
            
           /* RGB[0] = colors[3*bin+1];
            RGB[1] = colors[3*bin+1];
            RGB[2] = colors[3*bin]; */
            
            
           /* int red = 192;
            int green = 192;
            int blue = 192;
            float redIncrement = ((float) (192.0 - colors[3*bin+0]))/500;
            float greenIncrement = ((float) (192.0 - colors[3*bin+1]))/500;
            float blueIncrement = ((float) (192.0 - colors[3*bin+2]))/500;
            red -= (int) (redIncrement*frame);
            green -= (int) (greenIncrement*frame);
            blue -= (int) (blueIncrement*frame);
            RGB[0] = (unsigned char) red;
            RGB[1] = (unsigned char) green;
            RGB[2] = (unsigned char) blue; */
           // std::cout << redIncrement << std::endl;
           // std::cout << (int) red << std::endl;
           // std::cout << (int) green << std::endl;
           // std::cout << (int) blue << std::endl;
           // exit(EXIT_FAILURE);
          /*  RGB[0] = colors[3*bin+0];
            RGB[1] = colors[3*bin+1];
            RGB[2] = colors[3*bin+2]; */
           /* RGB[0] = 255;
            RGB[1] = 255;
            RGB[2] = 255; */
            
          /*  RGB[0] = 2;
            RGB[1] = 132;
            RGB[2] = 130; */
            
          /*  RGB[0] = 150;
            RGB[1] = 10;
            RGB[2] = 50; */
            
            opacity = .141*opacities[bin];
        }
    }
};

// Transfer function setup defined by professor

TransferFunction
SetupTransferFunction(void)
{
    int  i;
    
    TransferFunction rv;
    rv.min = 10;
    rv.max = 15;
    rv.numBins = 256;
    rv.colors = new unsigned char[3*256];
    rv.opacities = new double[256];
    unsigned char charOpacity[256] = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 13, 14, 14, 14, 14, 14, 14, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 5, 4, 3, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 17, 17, 17, 17, 17, 17, 16, 16, 15, 14, 13, 12, 11, 9, 8, 7, 6, 5, 5, 4, 3, 3, 3, 4, 5, 6, 7, 8, 9, 11, 12, 14, 16, 18, 20, 22, 24, 27, 29, 32, 35, 38, 41, 44, 47, 50, 52, 55, 58, 60, 62, 64, 66, 67, 68, 69, 70, 70, 70, 69, 68, 67, 66, 64, 62, 60, 58, 55, 52, 50, 47, 44, 41, 38, 35, 32, 29, 27, 24, 22, 20, 20, 23, 28, 33, 38, 45, 51, 59, 67, 76, 85, 95, 105, 116, 127, 138, 149, 160, 170, 180, 189, 198, 205, 212, 217, 221, 223, 224, 224, 222, 219, 214, 208, 201, 193, 184, 174, 164, 153, 142, 131, 120, 109, 99, 89, 79, 70, 62, 54, 47, 40, 35, 30, 25, 21, 17, 14, 12, 10, 8, 6, 5, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    
    for (i = 0 ; i < 256 ; i++)
        rv.opacities[i] = charOpacity[i]/255.0;
    const int numControlPoints = 8;
    unsigned char controlPointColors[numControlPoints*3] = {
        71, 71, 219, 0, 0, 91, 0, 255, 255, 0, 127, 0,
        255, 255, 0, 255, 96, 0, 107, 0, 0, 224, 76, 76
    };
    double controlPointPositions[numControlPoints] = { 0, 0.143, 0.285, 0.429, 0.571, 0.714, 0.857, 1.0 };
    for (i = 0 ; i < numControlPoints-1 ; i++)
    {
        int start = controlPointPositions[i]*rv.numBins;
        int end   = controlPointPositions[i+1]*rv.numBins+1;
      //  cerr << "Working on " << i << "/" << i+1 << ", with range " << start << "/" << end << endl;
        if (end >= rv.numBins)
            end = rv.numBins-1;
        for (int j = start ; j <= end ; j++)
        {
            double proportion = (j/(rv.numBins-1.0)-controlPointPositions[i])/(controlPointPositions[i+1]-controlPointPositions[i]);
            if (proportion < 0 || proportion > 1.)
                continue;
            for (int k = 0 ; k < 3 ; k++)
                rv.colors[3*j+k] = proportion*(controlPointColors[3*(i+1)+k]-controlPointColors[3*i+k])
                + controlPointColors[3*i+k];
        }
    }
   // std::cout << "finished generating transfer function" << std::endl;
    return rv;
}

// Basic VTK setup; copied from professor's example code
class vtk441Mapper : public vtkOpenGLPolyDataMapper
{
  protected:
   GLuint displayList;
   bool   initialized;

  public:
   vtk441Mapper()
   {
     initialized = false;
   }
    
   void
   RemoveVTKOpenGLStateSideEffects()
   {
     float Info[4] = { 0, 0, 0, 1 };
     glLightModelfv(GL_LIGHT_MODEL_AMBIENT, Info);
     float ambient[4] = { 1,1, 1, 1.0 };
     glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, ambient);
     float diffuse[4] = { 1, 1, 1, 1.0 };
     glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, diffuse);
     float specular[4] = { 1, 1, 1, 1.0 };
     glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, specular);
   }


   void SetupLight(void)
   {
       glEnable(GL_LIGHTING);
       glEnable(GL_LIGHT0);
       GLfloat diffuse0[4] = { 0.8, 0.8, 0.8, 1 };
       GLfloat ambient0[4] = { 0.2, 0.2, 0.2, 1 };
       GLfloat specular0[4] = { 0.0, 0.0, 0.0, 1 };
       GLfloat pos0[4] = { 1, 2, 3, 0 };
       glLightfv(GL_LIGHT0, GL_POSITION, pos0);
       glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse0);
       glLightfv(GL_LIGHT0, GL_AMBIENT, ambient0);
       glLightfv(GL_LIGHT0, GL_SPECULAR, specular0);
       glDisable(GL_LIGHT1);
       glDisable(GL_LIGHT2);
       glDisable(GL_LIGHT3);
       glDisable(GL_LIGHT5);
       glDisable(GL_LIGHT6);
       glDisable(GL_LIGHT7);
   }
};

// Everything beyond here written by me:

class vtk441MapperPart1 : public vtk441Mapper
{
 public:
   static vtk441MapperPart1 *New();
   
   virtual void RenderPiece(vtkRenderer *ren, vtkActor *act)
   {
       
       RemoveVTKOpenGLStateSideEffects();
       //SetupLight();
       vtkDataSetReader *rdr = vtkDataSetReader::New();
       rdr->SetFileName("astro512.vtk");
       cerr << "Reading" << endl;
       rdr->Update();
       cerr << "Done reading" << endl;
       if (rdr->GetOutput()->GetNumberOfCells() == 0)
       {
           cerr << "Unable to open file!!" << endl;
           exit(EXIT_FAILURE);
       }
       rdr->SetScalarsName("hardyglobal");
       
       int dims[3];
       vtkRectilinearGrid* grid = (vtkRectilinearGrid*) rdr->GetOutput();
       grid->GetDimensions(dims);
      
       // Get image data
       
       float *X = (float *) grid->GetXCoordinates()->GetVoidPointer(0);
       float *Y = (float *) grid->GetYCoordinates()->GetVoidPointer(0);
       float *Z = (float *) grid->GetZCoordinates()->GetVoidPointer(0);
       float *fieldValue = (float *) grid->GetPointData()->GetScalars()->GetVoidPointer(0);
       std::cout << dims[0] << std::endl;
       std::cout << dims[1] << std::endl;
       std::cout << dims[2] << std::endl;
       std::cout << X[0] << std::endl;
       
       vtkImageData *image = NewImage(1200, 600);
       unsigned char *buffer =
       (unsigned char *) image->GetScalarPointer(0,0,0);
       int npixels = 1200*600;
       
       GLuint texture;
       glGenTextures( 1, &texture );
       
       // Set up colors

       TransferFunction tf = SetupTransferFunction();
       unsigned char* RGBAColors = new unsigned char[4*dims[0]*dims[1]*dims[2]];
       
       double dummy = 0;
       unsigned char* transferColors = new unsigned char[3];
       
       transferColors[0] = 0;
       transferColors[1] = 0;
       transferColors[2] = 0;
       int count = 0;
       
       // Apply transfer function
       
       for (int index = 0; index < dims[0]*dims[1]*dims[2]; ++index){
           tf.ApplyTransferFunction(fieldValue[index],transferColors,dummy,0);
           RGBAColors[count]=transferColors[0];
           RGBAColors[count+1]=transferColors[1];
           RGBAColors[count+2]=transferColors[2];
           
           unsigned char opac = (unsigned char) (255.0*dummy);
           
           RGBAColors[count+3]=opac;
           count += 4;
           //count += 3;
       }
       
       //std::cout << "finished setting up colors" << std::endl;
       
       delete[] transferColors;
       
       // Set up and apply texture, and set up GL states
       
       glEnable(GL_TEXTURE_3D);
       glEnable (GL_BLEND);
       
       glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA, dims[0], dims[1], dims[2], 0, GL_RGBA,  GL_UNSIGNED_BYTE, RGBAColors);
       
       glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
       
       // For each (camera) frame:
       
       for (int frame = 0; frame < 300; ++frame) {
           //std::cout << "frame " << frame << std::endl;
           
           // Turn on textures and depth-testing
           glEnable(GL_TEXTURE_3D);

           glEnable(GL_DEPTH_TEST);
           glEnable (GL_BLEND);
           glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
           
           // For each color stream of each pixel:
           
           for (int i = 0 ; i < npixels*3 ; i++)
               buffer[i] = 0;
           
           
           // Plug in values to transfer function
           double dummy = 0;
           unsigned char* transferColors = new unsigned char[3];
           transferColors[0] = 0;
           transferColors[1] = 0;
           transferColors[2] = 0;
           int count = 0;
           for (int index = 0; index < dims[0]*dims[1]*dims[2]; ++index){
               tf.ApplyTransferFunction(fieldValue[index],transferColors,dummy,frame);
               RGBAColors[count]=transferColors[0];
               RGBAColors[count+1]=transferColors[1];
               RGBAColors[count+2]=transferColors[2];
               
               unsigned char opac = (unsigned char) (255.0*dummy);
               
               RGBAColors[count+3]=opac;
               count += 4;
               //count += 3;
           }
           
           delete[] transferColors;
      
           // Rotate frame
           
           double rotation = frame * ((double)360.0/500.0);
           glMatrixMode(GL_TEXTURE);
           glPushMatrix();
           glTranslatef(0.5,0.5,0.5);
          /* if (frame < 500){
               glRotatef(rotation,0,1,0);
           }
           else if (frame < 1000){
               glRotatef(rotation,1,1,1);
           } */
        
           glRotatef(rotation,1,.5,0);

           glTranslatef(-0.5,-0.5,-0.5);
       
           /*  glMatrixMode(GL_TEXTURE);
           glPushMatrix();
           glTranslatef(0.5,0.5,0.5);
           
           glRotatef(270,0,1,0);
           glTranslatef(-0.5,-0.5,-0.5); */
           
           // Reapply texture

            glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA, dims[0], dims[1], dims[2], 0, GL_RGBA,  GL_UNSIGNED_BYTE, RGBAColors);
  
           glEnable(GL_COLOR_MATERIAL);
           glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
           glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
           glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
           glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_BORDER);
           glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
           glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
           
           // Render each plane as a set of quads
       
           for (int x = 0; x < dims[0]; x+=3){
               //std::cout << "x-iteration " << x << std::endl;
               if ((x+3) >= dims[0]){
                   continue;
               }
               double xCoord1 = X[x]*(1.04085349986989*(pow(10,-6)))+31.5001821493625;
               double xCoord2 = X[x+3]*(1.04085349986989*(pow(10,-6)))+31.5001821493625;
               if (xCoord1 == xCoord2){
                   continue;
               }
               for (int y = 0; y < dims[1]; y+=3){
                   if ((y+3) >= dims[1]){
                       continue;
                   }
                   
                   double yCoord1 = Y[y]*(1.04085349986989*(pow(10,-6)))+31.5001821493625;
                   double yCoord2 = Y[y+3]*(1.04085349986989*(pow(10,-6)))+31.5001821493625;
                   
                   if (yCoord1 == yCoord2){
                       continue;
                   }
                   
                   for (float z = 0; z < dims[2]; z+=.25){
                       glBegin(GL_QUADS);
                       
                            double zCoord = z/8;
                           float texXcoord1 = ((float) x)/dims[0];
                           float texXcoord2 = ((float)(x+3))/dims[0];
                           float texYcoord1 = ((float)y)/dims[1];
                           float texYcoord2 = ((float)(y+3))/dims[1];
                           float texZcoord = ((float)z)/dims[2];
                           
                           glTexCoord3f(texXcoord1, texYcoord1, texZcoord);
                           glVertex3f(xCoord1,yCoord1,zCoord);
                           glTexCoord3f(texXcoord2, texYcoord1, texZcoord);
                           glVertex3f(xCoord2,yCoord1,zCoord);
                           glTexCoord3f(texXcoord2, texYcoord2, texZcoord);
                           glVertex3f(xCoord2,yCoord2,zCoord);
                           glTexCoord3f(texXcoord1, texYcoord2, texZcoord);
                           glVertex3f(xCoord1,yCoord2,zCoord);
                       
                       glEnd();
                   }
               }
           }
           
           glReadPixels(0,0,1200,600,GL_RGB,GL_UNSIGNED_BYTE,buffer);

           std::string picture = "c";
           std::stringstream s;
           s << picture << frame;
           std::string imageFile = s.str();
           WriteImage(image, imageFile.c_str());

         //  glBindTexture( GL_TEXTURE_3D, 0 );
           glDisable(GL_TEXTURE_3D);
           glPopMatrix();
           glFlush();
           glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
       }
   }
};

// VTK rendering window; modified code from professor's example

vtkStandardNewMacro(vtk441MapperPart1);

int main()
{
  // Dummy input so VTK pipeline mojo is happy.
  //
  vtkSmartPointer<vtkSphereSource> sphere =
    vtkSmartPointer<vtkSphereSource>::New();
  sphere->SetThetaResolution(100);
  sphere->SetPhiResolution(50);

  // The mapper is responsible for pushing the geometry into the graphics
  // library. It may also do color mapping, if scalars or other attributes
  // are defined. 
  //
  vtkSmartPointer<vtk441MapperPart1> win1Mapper =
    vtkSmartPointer<vtk441MapperPart1>::New();
  win1Mapper->SetInputConnection(sphere->GetOutputPort());

  vtkSmartPointer<vtkActor> win1Actor =
    vtkSmartPointer<vtkActor>::New();
  win1Actor->SetMapper(win1Mapper);

  vtkSmartPointer<vtkRenderer> ren1 =
    vtkSmartPointer<vtkRenderer>::New();

  vtkSmartPointer<vtkRenderWindow> renWin =
    vtkSmartPointer<vtkRenderWindow>::New();
  renWin->AddRenderer(ren1);
  ren1->SetViewport(0, 0, 1, 1);

  vtkSmartPointer<vtkRenderWindowInteractor> iren =
    vtkSmartPointer<vtkRenderWindowInteractor>::New();
  iren->SetRenderWindow(renWin);

  // Add the actors to the renderer, set the background and size.
  //
  bool doWindow1 = true;
  if (doWindow1)
     ren1->AddActor(win1Actor);
  ren1->SetBackground(0.0, 0.0, 0.0);
  renWin->SetSize(1200, 600);

  // Set up the lighting.
  //
  vtkRenderer *rens[1] = { ren1 };
 
  rens[0]->GetActiveCamera()->SetFocalPoint(32,32,32);
  rens[0]->GetActiveCamera()->SetPosition(70,50,110);
   // rens[0]->GetActiveCamera()->SetPosition(120,120,64);
  rens[0]->GetActiveCamera()->SetViewUp(0,1,0);
  //rens[0]->GetActiveCamera()->SetDistance(150);

  
  // This starts the event loop and invokes an initial render.
  //
  ((vtkInteractorStyle *)iren->GetInteractorStyle())->SetAutoAdjustCameraClippingRange(0);
  ((vtkInteractorStyle *)iren->GetInteractorStyle())->SetMouseWheelMotionFactor(.01);
  ((vtkInteractorStyleTrackballCamera *)iren->GetInteractorStyle())->SetMotionFactor(.000001);
  iren->Initialize();
  iren->Start();

  return EXIT_SUCCESS;
}




