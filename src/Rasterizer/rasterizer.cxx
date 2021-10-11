#include <iostream>
#include <vtkDataSet.h>
#include <vtkImageData.h>
#include <vtkPNGWriter.h>
#include <vtkPointData.h>
#include <vtkPolyData.h>
#include <vtkPolyDataReader.h>
#include <vtkPoints.h>
#include <vtkUnsignedCharArray.h>
#include <vtkFloatArray.h>
#include <vtkDoubleArray.h>
#include <vtkCellArray.h>
#include <vtkDataSetWriter.h>
#include <math.h>
#include <string>
#include <sstream>

using std::cerr;
using std::endl;

// Basic data structures and functions defined by professor
class Matrix
{
public:
    double          A[4][4];
    
    void            TransformPoint(const double *ptIn, double *ptOut);
    static Matrix   ComposeMatrices(const Matrix &, const Matrix &);
    void            Print(ostream &o);
};

void
Matrix::Print(ostream &o)
{
    for (int i = 0 ; i < 4 ; i++)
    {
        char str[256];
        sprintf(str, "(%.7f %.7f %.7f %.7f)\n", A[i][0], A[i][1], A[i][2], A[i][3]);
        o << str;
    }
}

Matrix
Matrix::ComposeMatrices(const Matrix &M1, const Matrix &M2)
{
    Matrix rv;
    for (int i = 0 ; i < 4 ; i++)
        for (int j = 0 ; j < 4 ; j++)
        {
            rv.A[i][j] = 0;
            for (int k = 0 ; k < 4 ; k++)
                rv.A[i][j] += M1.A[i][k]*M2.A[k][j];
        }
    
    return rv;
}

void
Matrix::TransformPoint(const double *ptIn, double *ptOut)
{
    ptOut[0] = ptIn[0]*A[0][0]
    + ptIn[1]*A[1][0]
    + ptIn[2]*A[2][0]
    + ptIn[3]*A[3][0];
    ptOut[1] = ptIn[0]*A[0][1]
    + ptIn[1]*A[1][1]
    + ptIn[2]*A[2][1]
    + ptIn[3]*A[3][1];
    ptOut[2] = ptIn[0]*A[0][2]
    + ptIn[1]*A[1][2]
    + ptIn[2]*A[2][2]
    + ptIn[3]*A[3][2];
    ptOut[3] = ptIn[0]*A[0][3]
    + ptIn[1]*A[1][3]
    + ptIn[2]*A[2][3]
    + ptIn[3]*A[3][3];
}

// Class defined by professor but transforms written by me
class Camera
{
public:
    double          near, far;
    double          angle;
    double          position[3];
    double          focus[3];
    double          up[3];
    
    // Define camera, view, and device transforms
    Matrix ViewTransform(void) {
        Matrix viewMatrix = Matrix::Matrix();
        for (int i = 0; i < 4; ++i){
            for (int j = 0; j < 4; ++j){
                viewMatrix.A[i][j] = 0;
            }
        }
        // Set up view transform matrix
        viewMatrix.A[0][0] = 1/(tan(angle/2));
        viewMatrix.A[1][1] = 1/(tan(angle/2));
        viewMatrix.A[2][2] = (far + near)/(far-near);
        viewMatrix.A[3][2] = (2*far*near)/(far-near);
        viewMatrix.A[2][3] = -1;
        return viewMatrix;
    }
    
    Matrix CameraTransform(void) {
        Matrix cameraMatrix = Matrix::Matrix();
        double *v1 = new double[3];
        double *v2 = new double[3];
        double *v3 = new double[3];
        double *t = new double[3];
        
        // Set v3 and t
        for (int i = 0; i < 3; ++i){
            cameraMatrix.A[i][3] = 0;
            v3[i] = position[i]-focus[i];
            t[i] = 0 - position[i];
        }
        double normV3 = sqrt(pow(v3[0],2)+pow(v3[1],2)+pow(v3[2],2));
        v3[0] = v3[0]/normV3;
        v3[1] = v3[1]/normV3;
        v3[2] = v3[2]/normV3;
        
        // Calculate cross-products to find v1 and v2
        cameraMatrix.A[3][3] = 1;
        v1[2] = (up[0]*v3[1])-(up[1]*v3[0]);
        v1[1] = (up[2]*v3[0])-(up[0]*v3[2]);
        v1[0] = (up[1]*v3[2])-(up[2]*v3[1]);
        
        v2[2] = (v3[0]*v1[1])-(v3[1]*v1[0]);
        v2[1] = (v3[2]*v1[0])-(v3[0]*v1[2]);
        v2[0] = (v3[1]*v1[2])-(v3[2]*v1[1]);
        
        double normV1 = sqrt(pow(v1[0],2)+pow(v1[1],2)+pow(v1[2],2));
        double normV2 = sqrt(pow(v2[0],2)+pow(v2[1],2)+pow(v2[2],2));
        
        v1[0] /= normV1;
        v1[1] /= normV1;
        v1[2] /= normV1;
        
        v2[0] /= normV2;
        v2[1] /= normV2;
        v2[2] /= normV2;
        
        // Set up matrix
        for (int i = 0; i < 3; ++i){
            cameraMatrix.A[i][0] = v1[i];
            cameraMatrix.A[i][1] = v2[i];
            cameraMatrix.A[i][2] = v3[i];
        }
        cameraMatrix.A[3][0] = (v1[0]*t[0])+(v1[1]*t[1])+(v1[2]*t[2]);
        cameraMatrix.A[3][1] = (v2[0]*t[0])+(v2[1]*t[1])+(v2[2]*t[2]);
        cameraMatrix.A[3][2] = (v3[0]*t[0])+(v3[1]*t[1])+(v3[2]*t[2]);
        delete[] v1;
        delete[] v2;
        delete[] v3;
        delete[] t;
        
        return cameraMatrix;
        
    }
    Matrix DeviceTransform(void) {
        Matrix deviceMatrix = Matrix::Matrix();
        for (int i = 0; i < 4; ++i){
            for (int j = 0; j < 4; ++j){
                deviceMatrix.A[i][j] = 0;
            }
        }
        // Set up device transform matrix
        deviceMatrix.A[3][3] = 1;
        deviceMatrix.A[3][0] = 500;
        deviceMatrix.A[0][0] = 500;
        deviceMatrix.A[3][1] = 500;
        deviceMatrix.A[1][1] = 500;
        deviceMatrix.A[2][2] = 1;
        return deviceMatrix;
    }
};

// Function defined by professor
double SineParameterize(int curFrame, int nFrames, int ramp)
{
    int nNonRamp = nFrames-2*ramp;
    double height = 1./(nNonRamp + 4*ramp/M_PI);
    if (curFrame < ramp)
    {
        double factor = 2*height*ramp/M_PI;
        double eval = cos(M_PI/2*((double)curFrame)/ramp);
        return (1.-eval)*factor;
    }
    else if (curFrame > nFrames-ramp)
    {
        int amount_left = nFrames-curFrame;
        double factor = 2*height*ramp/M_PI;
        double eval =cos(M_PI/2*((double)amount_left/ramp));
        return 1. - (1-eval)*factor;
    }
    double amount_in_quad = ((double)curFrame-ramp);
    double quad_part = amount_in_quad*height;
    double curve_part = height*(2*ramp)/M_PI;
    return quad_part+curve_part;
}

// More data structures and functions defined by professor
Camera
GetCamera(int frame, int nframes)
{
    double t = SineParameterize(frame, nframes, nframes/10);
    Camera c;
    c.near = 5;
    c.far = 200;
    c.angle = M_PI/6;
    c.position[0] = 40*sin(2*M_PI*t);
    c.position[1] = 40*cos(2*M_PI*t);
    c.position[2] = 40;
    c.focus[0] = 0;
    c.focus[1] = 0;
    c.focus[2] = 0;
    c.up[0] = 0;
    c.up[1] = 1;
    c.up[2] = 0;
    return c;
}

struct LightingParameters
{
    LightingParameters(void)
    {
        lightDir[0] = 0.6;
        lightDir[1] = 0;
        lightDir[2] = 0.8;
        Ka = 0.3;
        Kd = 0.7;
        Ks = 5.3;
        alpha = 7.5;
    };
    
    
    double lightDir[3]; // The direction of the light source
    double Ka;           // The coefficient for ambient lighting.
    double Kd;           // The coefficient for diffuse lighting.
    double Ks;           // The coefficient for specular lighting.
    double alpha;        // The exponent term for specular lighting.
};

LightingParameters lp;

double ceil441(double f)
{
    return ceil(f-0.00001);
}

double floor441(double f)
{
    return floor(f+0.00001);
}


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

class Triangle
{
  public:
      double         X[3];
      double         Y[3];
      double         Z[3];
      double         colors[3][3];
      double         normals[3][3];
    
      void setX(int index, double newVal){
          X[index] = newVal;
      }
      void setY(int index, double newVal){
          Y[index] = newVal;
      }
      void setZ(int index, double newVal){
          Z[index] = newVal;
      }

  // would some methods for transforming the triangle in place be helpful?
};

class Screen
{
  public:
      unsigned char   *buffer;
      int width, height;

  // would some methods for accessing and setting pixels be helpful?
};


// Routine to get number of triangles
double getNum(void){
    vtkPolyDataReader *rdr = vtkPolyDataReader::New();
    rdr->SetFileName("proj1e_geometry.vtk");
    cerr << "Reading" << endl;
    rdr->Update();
    if (rdr->GetOutput()->GetNumberOfCells() == 0)
    {
        cerr << "Unable to open file!!" << endl;
        exit(EXIT_FAILURE);
    }
    vtkPolyData *pd = rdr->GetOutput();
    int numTris = pd->GetNumberOfCells();
    return numTris;
}

std::vector<Triangle>
GetTriangles(void)
{
    vtkPolyDataReader *rdr = vtkPolyDataReader::New();
    rdr->SetFileName("proj1e_geometry.vtk");
    cerr << "Reading" << endl;
    rdr->Update();
    cerr << "Done reading" << endl;
    if (rdr->GetOutput()->GetNumberOfCells() == 0)
    {
        cerr << "Unable to open file!!" << endl;
        exit(EXIT_FAILURE);
    }
    vtkPolyData *pd = rdr->GetOutput();
    /*
     vtkDataSetWriter *writer = vtkDataSetWriter::New();
     writer->SetInput(pd);
     writer->SetFileName("hrc.vtk");
     writer->Write();
     */
    
    int numTris = pd->GetNumberOfCells();
    vtkPoints *pts = pd->GetPoints();
    vtkCellArray *cells = pd->GetPolys();
    vtkDoubleArray *var = (vtkDoubleArray *) pd->GetPointData()->GetArray("hardyglobal");
    double *color_ptr = var->GetPointer(0);
    //vtkFloatArray *var = (vtkFloatArray *) pd->GetPointData()->GetArray("hardyglobal");
    //float *color_ptr = var->GetPointer(0);
    vtkFloatArray *n = (vtkFloatArray *) pd->GetPointData()->GetNormals();
    float *normals = n->GetPointer(0);
    std::vector<Triangle> tris(numTris);
    vtkIdType npts;
    vtkIdType *ptIds;
    int idx;
    for (idx = 0, cells->InitTraversal() ; cells->GetNextCell(npts, ptIds) ; idx++)
    {
        if (npts != 3)
        {
            cerr << "Non-triangles!! ???" << endl;
            exit(EXIT_FAILURE);
        }
        double *pt = NULL;
        pt = pts->GetPoint(ptIds[0]);
        tris[idx].X[0] = pt[0];
        tris[idx].Y[0] = pt[1];
        tris[idx].Z[0] = pt[2];
        tris[idx].normals[0][0] = normals[3*ptIds[0]+0];
        tris[idx].normals[0][1] = normals[3*ptIds[0]+1];
        tris[idx].normals[0][2] = normals[3*ptIds[0]+2];
        pt = pts->GetPoint(ptIds[1]);
        tris[idx].X[1] = pt[0];
        tris[idx].Y[1] = pt[1];
        tris[idx].Z[1] = pt[2];
        tris[idx].normals[1][0] = normals[3*ptIds[1]+0];
        tris[idx].normals[1][1] = normals[3*ptIds[1]+1];
        tris[idx].normals[1][2] = normals[3*ptIds[1]+2];
        pt = pts->GetPoint(ptIds[2]);
        tris[idx].X[2] = pt[0];
        tris[idx].Y[2] = pt[1];
        tris[idx].Z[2] = pt[2];
        tris[idx].normals[2][0] = normals[3*ptIds[2]+0];
        tris[idx].normals[2][1] = normals[3*ptIds[2]+1];
        tris[idx].normals[2][2] = normals[3*ptIds[2]+2];
        
        // 1->2 interpolate between light blue, dark blue
        // 2->2.5 interpolate between dark blue, cyan
        // 2.5->3 interpolate between cyan, green
        // 3->3.5 interpolate between green, yellow
        // 3.5->4 interpolate between yellow, orange
        // 4->5 interpolate between orange, brick
        // 5->6 interpolate between brick, salmon
        double mins[7] = { 1, 2, 2.5, 3, 3.5, 4, 5 };
        double maxs[7] = { 2, 2.5, 3, 3.5, 4, 5, 6 };
        unsigned char RGB[8][3] = { { 71, 71, 219 },
            { 0, 0, 91 },
            { 0, 255, 255 },
            { 0, 128, 0 },
            { 255, 255, 0 },
            { 255, 96, 0 },
            { 107, 0, 0 },
            { 224, 76, 76 }
        };
        for (int j = 0 ; j < 3 ; j++)
        {
            float val = color_ptr[ptIds[j]];
            int r;
            for (r = 0 ; r < 7 ; r++)
            {
                if (mins[r] <= val && val < maxs[r])
                    break;
            }
            if (r == 7)
            {
                cerr << "Could not interpolate color for " << val << endl;
                exit(EXIT_FAILURE);
            }
            double proportion = (val-mins[r]) / (maxs[r]-mins[r]);
            tris[idx].colors[j][0] = (RGB[r][0]+proportion*(RGB[r+1][0]-RGB[r][0]))/255.0;
            tris[idx].colors[j][1] = (RGB[r][1]+proportion*(RGB[r+1][1]-RGB[r][1]))/255.0;
            tris[idx].colors[j][2] = (RGB[r][2]+proportion*(RGB[r+1][2]-RGB[r][2]))/255.0;
        }
    }
    
    return tris;
}

// All code following this point written by me

// This implements the interpolation algorithm
double interpolate(double firstLocation, double secondLocation, double intermediateLocation, double firstFieldValue, double secondFieldValue){
    
    double diff1 = intermediateLocation - firstLocation;
    double diff2 = secondLocation - firstLocation;
    double diff3 = secondFieldValue-firstFieldValue;
    double result = firstFieldValue + ((diff1/diff2)*diff3);
    return result;
    
}


// Method to calculate the shading factor
double shadingFactor(double firstN,double secondN,double thirdN, LightingParameters *lighting, Camera cam){
    
    double ambient = lighting->Ka;
    double dotProduct1 = (firstN * (lighting->lightDir[0])) + (secondN * (lighting->lightDir[1])) + (thirdN * (lighting->lightDir[2]));

    double diffuse = lighting->Kd * dotProduct1;
    
    if (diffuse < 0){
        diffuse = -1*diffuse;
    }
    
    double view1 = cam.position[0]-cam.focus[0];
    double view2 = cam.position[1]-cam.focus[1];
    double view3 = cam.position[2]-cam.focus[2];
    double viewNorm = sqrt(pow(view1,2)+pow(view2,2)+pow(view3,2));
    view1 /= viewNorm;
    view2 /= viewNorm;
    view3 /= viewNorm;
    
    double R1 = (2*dotProduct1)*firstN - (lighting->lightDir[0]);
    double R2 = (2*dotProduct1)*secondN - (lighting->lightDir[1]);
    double R3 = (2*dotProduct1)*thirdN - (lighting->lightDir[2]);
    
    double dotProduct2 = view1*R1 + view2*R2 + view3*R3;
    
    double exponent = lighting->alpha;
    double specular = lighting->Ks * pow(dotProduct2,exponent);
    if ((specular < 0) || (specular != specular)){
        specular = 0;
    }

    double result = ambient + diffuse + specular;
    return result;
}

// This is the principal rasterization algorithm, equipped to handle both flat-topped and flat-bottomed triangles

void flatTriangle(char c, double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2, double c0[3],double c1[3], double c2[3], double N0[3], double N1[3], double N2[3], unsigned char* buffer, double **zbuffer, LightingParameters *lights, Camera cam){
    
    // Set up variables for vertex coordinates, colors, and normals
    double firstVx = 0;
    double firstVy = 0;
    double secondVx = 0;
    double secondVy = 0;
    double thirdVx = 0;
    double thirdVy = 0;
    
    double firstZ = -1;
    double secondZ = -1;
    double thirdZ = -1;
    
    double* firstColor = new double[3];
    double* secondColor = new double[3];
    double* thirdColor = new double[3];
    double* firstNormal = new double[3];
    double* secondNormal = new double[3];
    double* thirdNormal = new double[3];

    
    
    /* This scheme fixes the second vertex as the apex of the triangle if it is
    flat-bottomed and the lowest point if it is flat-topped, with vertices one and
    three as the corners on the base. */
    
    // This case is general to both
    if (y0 == y1){
        secondVx = x2;
        secondVy = y2;
        secondZ = z2;
        secondColor[0] = c2[0];
        secondColor[1] = c2[1];
        secondColor[2] = c2[2];
        secondNormal[0] = N2[0];
        secondNormal[1] = N2[1];
        secondNormal[2] = N2[2];
        if (x0 > x1){
            firstVx = x1;
            firstVy = y1;
            firstZ = z1;
            firstColor[0] = c1[0];
            firstColor[1] = c1[1];
            firstColor[2] = c1[2];
            firstNormal[0] = N1[0];
            firstNormal[1] = N1[1];
            firstNormal[2] = N1[2];
            thirdVx = x0;
            thirdVy = y0;
            thirdZ = z0;
            thirdColor[0] = c0[0];
            thirdColor[1] = c0[1];
            thirdColor[2] = c0[2];
            thirdNormal[0] = N0[0];
            thirdNormal[1] = N0[1];
            thirdNormal[2] = N0[2];
        }
        else {
            firstVx = x0;
            firstVy = y0;
            firstZ = z0;
            firstColor[0] = c0[0];
            firstColor[1] = c0[1];
            firstColor[2] = c0[2];
            firstNormal[0] = N0[0];
            firstNormal[1] = N0[1];
            firstNormal[2] = N0[2];
            thirdVx = x1;
            thirdVy = y1;
            thirdZ = z1;
            thirdColor[0] = c1[0];
            thirdColor[1] = c1[1];
            thirdColor[2] = c1[2];
            thirdNormal[0] = N1[0];
            thirdNormal[1] = N1[1];
            thirdNormal[2] = N1[2];
        }
    }
    
    else if (y0 > y1){
        // If flat-bottomed
        if (c == 'b'){
            secondVx = x0;
            secondVy = y0;
            secondZ = z0;
            secondColor[0] = c0[0];
            secondColor[1] = c0[1];
            secondColor[2] = c0[2];
            secondNormal[0] = N0[0];
            secondNormal[1] = N0[1];
            secondNormal[2] = N0[2];
            if (x2 > x1){
                firstVx = x1;
                firstVy = y1;
                firstZ = z1;
                firstColor[0] = c1[0];
                firstColor[1] = c1[1];
                firstColor[2] = c1[2];
                firstNormal[0] = N1[0];
                firstNormal[1] = N1[1];
                firstNormal[2] = N1[2];
                thirdVx = x2;
                thirdVy = y2;
                thirdZ = z2;
                thirdColor[0] = c2[0];
                thirdColor[1] = c2[1];
                thirdColor[2] = c2[2];
                thirdNormal[0] = N2[0];
                thirdNormal[1] = N2[1];
                thirdNormal[2] = N2[2];
            }
            else {
                firstVx = x2;
                firstVy = y2;
                firstZ = z2;
                firstColor[0] = c2[0];
                firstColor[1] = c2[1];
                firstColor[2] = c2[2];
                firstNormal[0] = N2[0];
                firstNormal[1] = N2[1];
                firstNormal[2] = N2[2];
                thirdVx = x1;
                thirdVy = y1;
                thirdZ = z1;
                thirdColor[0] = c1[0];
                thirdColor[1] = c1[1];
                thirdColor[2] = c1[2];
                thirdNormal[0] = N1[0];
                thirdNormal[1] = N1[1];
                thirdNormal[2] = N1[2];
            }
        }
        // If flat-topped
        else if (c == 't'){
            secondVx = x1;
            secondVy = y1;
            secondZ = z1;
            secondColor[0] = c1[0];
            secondColor[1] = c1[1];
            secondColor[2] = c1[2];
            secondNormal[0] = N1[0];
            secondNormal[1] = N1[1];
            secondNormal[2] = N1[2];
            if (x2 > x0){
                firstVx = x0;
                firstVy = y0;
                firstZ = z0;
                firstColor[0] = c0[0];
                firstColor[1] = c0[1];
                firstColor[2] = c0[2];
                firstNormal[0] = N0[0];
                firstNormal[1] = N0[1];
                firstNormal[2] = N0[2];
                thirdVx = x2;
                thirdVy = y2;
                thirdZ = z2;
                thirdColor[0] = c2[0];
                thirdColor[1] = c2[1];
                thirdColor[2] = c2[2];
                thirdNormal[0] = N2[0];
                thirdNormal[1] = N2[1];
                thirdNormal[2] = N2[2];
            }
            else {
                firstVx = x2;
                firstVy = y2;
                firstZ = z2;
                firstColor[0] = c2[0];
                firstColor[1] = c2[1];
                firstColor[2] = c2[2];
                firstNormal[0] = N2[0];
                firstNormal[1] = N2[1];
                firstNormal[2] = N2[2];
                thirdVx = x0;
                thirdVy = y0;
                thirdZ = z0;
                thirdColor[0] = c0[0];
                thirdColor[1] = c0[1];
                thirdColor[2] = c0[2];
                thirdNormal[0] = N0[0];
                thirdNormal[1] = N0[1];
                thirdNormal[2] = N0[2];
            }
        }
    }
    
    else {
        // If flat-bottomed
        if (c == 'b'){
            secondVx = x1;
            secondVy = y1;
            secondZ = z1;
            secondColor[0] = c1[0];
            secondColor[1] = c1[1];
            secondColor[2] = c1[2];
            secondNormal[0] = N1[0];
            secondNormal[1] = N1[1];
            secondNormal[2] = N1[2];
            if (x2 > x0){
                firstVx = x0;
                firstVy = y0;
                firstZ = z0;
                firstColor[0] = c0[0];
                firstColor[1] = c0[1];
                firstColor[2] = c0[2];
                firstNormal[0] = N0[0];
                firstNormal[1] = N0[1];
                firstNormal[2] = N0[2];
                thirdVx = x2;
                thirdVy = y2;
                thirdZ = z2;
                thirdColor[0] = c2[0];
                thirdColor[1] = c2[1];
                thirdColor[2] = c2[2];
                thirdNormal[0] = N2[0];
                thirdNormal[1] = N2[1];
                thirdNormal[2] = N2[2];
            }
            else {
                firstVx = x2;
                firstVy = y2;
                firstZ = z2;
                firstColor[0] = c2[0];
                firstColor[1] = c2[1];
                firstColor[2] = c2[2];
                firstNormal[0] = N2[0];
                firstNormal[1] = N2[1];
                firstNormal[2] = N2[2];
                thirdVx = x0;
                thirdVy = y0;
                thirdZ = z0;
                thirdColor[0] = c0[0];
                thirdColor[1] = c0[1];
                thirdColor[2] = c0[2];
                thirdNormal[0] = N0[0];
                thirdNormal[1] = N0[1];
                thirdNormal[2] = N0[2];
            }
        }
        // If flat-topped
        else if (c == 't'){
            secondVx = x0;
            secondVy = y0;
            secondZ = z0;
            secondColor[0] = c0[0];
            secondColor[1] = c0[1];
            secondColor[2] = c0[2];
            secondNormal[0] = N0[0];
            secondNormal[1] = N0[1];
            secondNormal[2] = N0[2];
            if (x2 > x1){
                firstVx = x1;
                firstVy = y1;
                firstZ = z1;
                firstColor[0] = c1[0];
                firstColor[1] = c1[1];
                firstColor[2] = c1[2];
                firstNormal[0] = N1[0];
                firstNormal[1] = N1[1];
                firstNormal[2] = N1[2];
                thirdVx = x2;
                thirdVy = y2;
                thirdZ = z2;
                thirdColor[0] = c2[0];
                thirdColor[1] = c2[1];
                thirdColor[2] = c2[2];
                thirdNormal[0] = N2[0];
                thirdNormal[1] = N2[1];
                thirdNormal[2] = N2[2];
            }
            else {
                firstVx = x2;
                firstVy = y2;
                firstZ = z2;
                firstColor[0] = c2[0];
                firstColor[1] = c2[1];
                firstColor[2] = c2[2];
                firstNormal[0] = N2[0];
                firstNormal[1] = N2[1];
                firstNormal[2] = N2[2];
                thirdVx = x1;
                thirdVy = y1;
                thirdZ = z1;
                thirdColor[0] = c1[0];
                thirdColor[1] = c1[1];
                thirdColor[2] = c1[2];
                thirdNormal[0] = N1[0];
                thirdNormal[1] = N1[1];
                thirdNormal[2] = N1[2];
            }
        }
    }
    
    // Assign rowMin and rowMax based on vertex y-coordinates
    double rowMin = 0;
    double rowMax = 0;
    
    if (c == 'b'){
        rowMin = ceil441(firstVy);
        rowMax = floor441(secondVy);
    }
    else if (c == 't'){
        rowMin = ceil441(secondVy);
        rowMax = floor441(firstVy);
    }
    
    /* If the entire triangle is out of bounds, as in this first case, we 
     set rowMin > rowMax so that the loop iterating over rows will never execute
     and we will pass over that triangle entirely.  If only part of the triangle 
     is out of bounds, we truncate it. */
    if ((rowMax > 999) && (rowMin > 999)){
        rowMin = 1;
        rowMax = 0;
    }
    else if (rowMax > 999){
        rowMax = 999;
    }
    else if ((rowMax < 0) && (rowMin < 0)){
        rowMin = 1;
        rowMax = 0;
    }
    else if (rowMax < 0){
        rowMax = 0;
    }
    else if (rowMin < 0){
        rowMin = 0;
    }
    else if (rowMin > 999){
        rowMin = 999;
    }
    
    
    
    // This simplifies the following logic
    
    // Find the slopes and constants
    double diff11 = secondVy-firstVy;
    double diff12 = secondVx-firstVx;
    double slope1 = diff11/diff12;
    double constant1 = (-secondVx*slope1)+secondVy;
    
    double diff21 = thirdVy-secondVy;
    double diff22 = thirdVx-secondVx;
    double slope2 = diff21/diff22;
    double constant2 = (-thirdVx*slope2)+thirdVy;
    
    // Declare variables for left and right endpoints of row
    double leftEnd;
    double rightEnd;
    // Iterate over rows triangle can occupy
    
    // Define variables we will use for z-values and colors at endpoints
    double zLeft;
    double zRight;
    double cLeft0;
    double cLeft1;
    double cLeft2;
    double cRight0;
    double cRight1;
    double cRight2;
    double NLeft0;
    double NLeft1;
    double NLeft2;
    double NRight0;
    double NRight1;
    double NRight2;
    
    for (int row = rowMin; row <= rowMax; ++row){
        
        // This section handles finding interpolation values and assigning them to the above variables and dealing with boundary issues
        
        // Handles one case of right triangles
        if (firstVx == secondVx){
            leftEnd = firstVx;
            // Need to interpolate by rows
            zLeft = interpolate(firstVy, secondVy, row, firstZ, secondZ);
            cLeft0 = interpolate(firstVy, secondVy, row, firstColor[0], secondColor[0]);
            cLeft1 = interpolate(firstVy, secondVy, row, firstColor[1], secondColor[1]);
            cLeft2 = interpolate(firstVy, secondVy, row, firstColor[2], secondColor[2]);
            NLeft0 = interpolate(firstVy, secondVy, row, firstNormal[0], secondNormal[0]);
            NLeft1 = interpolate(firstVy, secondVy, row, firstNormal[1], secondNormal[1]);
            NLeft2 = interpolate(firstVy, secondVy, row, firstNormal[2], secondNormal[2]);
        }
        else {
            leftEnd = ((row-constant1)/slope1);
            zLeft = interpolate(firstVx, secondVx, leftEnd, firstZ, secondZ);
            cLeft0 = interpolate(firstVx, secondVx, leftEnd, firstColor[0], secondColor[0]);
            cLeft1 = interpolate(firstVx, secondVx, leftEnd, firstColor[1], secondColor[1]);
            cLeft2 = interpolate(firstVx, secondVx, leftEnd, firstColor[2], secondColor[2]);
            NLeft0 = interpolate(firstVx, secondVx, leftEnd, firstNormal[0], secondNormal[0]);
            NLeft1 = interpolate(firstVx, secondVx, leftEnd, firstNormal[1], secondNormal[1]);
            NLeft2 = interpolate(firstVx, secondVx, leftEnd, firstNormal[2], secondNormal[2]);
        }
        
        // Truncates triangle if vertices go out of bounds
        double trueLeft = 0;
        if (leftEnd < 0){
            trueLeft = 0;
        }
        // Apply floor and ceiling and multiply by three color streams
        else {
            trueLeft = ceil441(leftEnd)*3;
        }
        
        // Handles other case of right triangles
        if (secondVx == thirdVx){
            rightEnd = secondVx;
            // Need to interpolate by row
            zRight = interpolate(secondVy, thirdVy, row, secondZ, thirdZ); 
            cRight0 = interpolate(secondVy, thirdVy, row, secondColor[0], thirdColor[0]);
            cRight1 = interpolate(secondVy, thirdVy, row, secondColor[1], thirdColor[1]);
            cRight2 = interpolate(secondVy, thirdVy, row, secondColor[2], thirdColor[2]);
            NRight0 = interpolate(secondVy, thirdVy, row, secondNormal[0], thirdNormal[0]);
            NRight1 = interpolate(secondVy, thirdVy, row, secondNormal[1], thirdNormal[1]);
            NRight2 = interpolate(secondVy, thirdVy, row, secondNormal[2], thirdNormal[2]);
        }
        else {
            rightEnd = ((row-constant2)/slope2);
            zRight = interpolate(secondVx, thirdVx, rightEnd, secondZ, thirdZ);
            cRight0 = interpolate(secondVx, thirdVx, rightEnd, secondColor[0], thirdColor[0]);
            cRight1 = interpolate(secondVx, thirdVx, rightEnd, secondColor[1], thirdColor[1]);
            cRight2 = interpolate(secondVx, thirdVx, rightEnd, secondColor[2], thirdColor[2]);
            NRight0 = interpolate(secondVx, thirdVx, rightEnd, secondNormal[0], thirdNormal[0]);
            NRight1 = interpolate(secondVx, thirdVx, rightEnd, secondNormal[1], thirdNormal[1]);
            NRight2 = interpolate(secondVx, thirdVx, rightEnd, secondNormal[2], thirdNormal[2]);
        }
        
        // Truncates triangle if vertices go out of bounds
        double trueRight = 0;
        if (rightEnd > 999){
            trueRight = 3*999;
        }
        // Apply floor and ceiling and multiply by three color streams
        else {
            trueRight = floor441(rightEnd)*3;
        }
        
        // Accounts for pixels in previous rows
        unsigned int offset = row*3000;
        
        // Iterate over pixels and set colors
        
        double pixelZ = -1;
        double pixelC0 = 0;
        double pixelC1 = 0;
        double pixelC2 = 0;
        double pixelN0 = 1;
        double pixelN1 = 0;
        double pixelN2 = 0;
        
        double shadingF = 0;
        
        for (int pixel = trueLeft; pixel <= trueRight; pixel+=3){
            // Find pixel Z and color values
            pixelZ = interpolate(leftEnd,rightEnd,pixel/3,zLeft,zRight);
            pixelC0 = interpolate(leftEnd,rightEnd,pixel/3,cLeft0,cRight0);
            pixelC1 = interpolate(leftEnd,rightEnd,pixel/3,cLeft1,cRight1);
            pixelC2 = interpolate(leftEnd,rightEnd,pixel/3,cLeft2,cRight2);
            pixelN0 = interpolate(leftEnd,rightEnd,pixel/3,NLeft0,NRight0);
            pixelN1 = interpolate(leftEnd,rightEnd,pixel/3,NLeft1,NRight1);
            pixelN2 = interpolate(leftEnd,rightEnd,pixel/3,NLeft2,NRight2);

            if (zbuffer[row][pixel/3] < pixelZ){
                zbuffer[row][pixel/3] = pixelZ;
                // Calculate shading factor
                shadingF = shadingFactor(pixelN0,pixelN1,pixelN2,lights, cam);
                // Conditional ensures color streams stay within bounds
                if (ceil441(shadingF*pixelC0*255.0) <= 255.0){
                    buffer[offset+pixel]=(unsigned char) (ceil441(shadingF*pixelC0*255.0));
                }
                else {
                    buffer[offset+pixel]=255;
                }
                if (ceil441(shadingF*pixelC1*255.0) <= 255.0){
                    buffer[offset+pixel+1]=(unsigned char) (ceil441(shadingF*pixelC1*255.0));
                }
                else {
                    buffer[offset+pixel+1]=255;
                }
                if (ceil441(shadingF*pixelC2*255.0) <= 255.0){
                    buffer[offset+pixel+2]=(unsigned char) (ceil441(shadingF*pixelC2*255.0));
                }
                else {
                    buffer[offset+pixel+2]=255;
                }
            }
        }
    }
    
    delete[] firstColor;
    delete[] secondColor;
    delete[] thirdColor;
    delete[] firstNormal;
    delete[] secondNormal;
    delete[] thirdNormal;
    
}


// Routine to divide non-flat triangle into two flat-triangles
void splitTriangle(Triangle t, unsigned char* buffer, double **zbuffer, LightingParameters *lights, Camera cam){
    
    double breakPointY = 0;
    double otherVx = 0;
    double slope = 0;
    double constant = 0;
    
    double otherZ = -1;
    
    double *otherColors = new double[3];
    double *otherNormal = new double[3];
    
    /* The logic in each of these cases is the same. We find the vertex with the
     intermediate y-coordinate, and use the slope on the other side of the 
     triangle to find the corresponding point on the triangle when we cut it 
     in two horizontally.  Then depending on which of the other two original 
     is higher, we call our routine for flat triangles twice - once flagged for 
     a flat-bottomed triangle and once for a flat-topped triangle. New to this 
     assignment, we also find the Z, normal, and color values at the new vertex
     using interpolation. */
    //std::cout << "start of splitting conditionals" << std::endl;
    if (((t.Y[0] < t.Y[1]) && (t.Y[1] < t.Y[2])) || ((t.Y[2] < t.Y[1]) && (t.Y[1] < t.Y[0]))){
        breakPointY = t.Y[1];
        if (t.X[2] != t.X[0]){
            slope = (t.Y[2]-t.Y[0])/(t.X[2]-t.X[0]);
            constant = (-t.X[2]*slope)+t.Y[2];
            otherVx = (breakPointY-constant)/slope;
            otherZ = interpolate(t.X[0],t.X[2],otherVx,t.Z[0],t.Z[2]);
            otherColors[0] = interpolate(t.X[0],t.X[2],otherVx,t.colors[0][0],t.colors[2][0]);
            otherColors[1] = interpolate(t.X[0],t.X[2],otherVx,t.colors[0][1],t.colors[2][1]);
            otherColors[2] = interpolate(t.X[0],t.X[2],otherVx,t.colors[0][2],t.colors[2][2]);
            otherNormal[0] = interpolate(t.X[0],t.X[2],otherVx,t.normals[0][0],t.normals[2][0]);
            otherNormal[1] = interpolate(t.X[0],t.X[2],otherVx,t.normals[0][1],t.normals[2][1]);
            otherNormal[2] = interpolate(t.X[0],t.X[2],otherVx,t.normals[0][2],t.normals[2][2]);
        }
        else {
            otherVx = t.X[2];
            otherZ = interpolate(t.Y[0],t.Y[2],breakPointY,t.Z[0],t.Z[2]);
            otherColors[0] = interpolate(t.Y[0],t.Y[2],breakPointY,t.colors[0][0],t.colors[2][0]);
            otherColors[1] = interpolate(t.Y[0],t.Y[2],breakPointY,t.colors[0][1],t.colors[2][1]);
            otherColors[2] = interpolate(t.Y[0],t.Y[2],breakPointY,t.colors[0][2],t.colors[2][2]);
            otherNormal[0] = interpolate(t.Y[0],t.Y[2],breakPointY,t.normals[0][0],t.normals[2][0]);
            otherNormal[1] = interpolate(t.Y[0],t.Y[2],breakPointY,t.normals[0][1],t.normals[2][1]);
            otherNormal[2] = interpolate(t.Y[0],t.Y[2],breakPointY,t.normals[0][2],t.normals[2][2]);
        }
        
        if (t.Y[0] < t.Y[2]){
            // t.Y[2] for flat bottom, t.Y[0] for flat top
            flatTriangle('b',t.X[1],otherVx,t.X[2],breakPointY,breakPointY,t.Y[2],t.Z[1],otherZ,t.Z[2],t.colors[1],otherColors,t.colors[2],t.normals[1],otherNormal,t.normals[2],buffer, zbuffer, lights, cam);
            flatTriangle('t',t.X[1],otherVx,t.X[0],breakPointY,breakPointY,t.Y[0],t.Z[1],otherZ,t.Z[0],t.colors[1],otherColors,t.colors[0],t.normals[1],otherNormal,t.normals[0],buffer, zbuffer, lights, cam);
        }
        else if (t.Y[2] < t.Y[0]){
            flatTriangle('b',t.X[1],otherVx,t.X[0],breakPointY,breakPointY,t.Y[0],t.Z[1],otherZ,t.Z[0],t.colors[1],otherColors,t.colors[0],t.normals[1],otherNormal,t.normals[0],buffer, zbuffer, lights, cam);
            flatTriangle('t',t.X[1],otherVx,t.X[2],breakPointY,breakPointY,t.Y[2],t.Z[1],otherZ,t.Z[2],t.colors[1],otherColors,t.colors[2],t.normals[1],otherNormal,t.normals[2],buffer, zbuffer, lights, cam);
        }
    }
    else if (((t.Y[0] < t.Y[2]) && (t.Y[2] < t.Y[1])) || ((t.Y[1] < t.Y[2]) && (t.Y[2] < t.Y[0]))){
        breakPointY = t.Y[2];
        if (t.X[1] != t.X[0]){
            slope = (t.Y[1]-t.Y[0])/(t.X[1]-t.X[0]);
            constant = (-t.X[1]*slope)+t.Y[1];
            otherVx = (breakPointY-constant)/slope;
            otherZ = interpolate(t.X[0],t.X[1],otherVx,t.Z[0],t.Z[1]);
            otherColors[0] = interpolate(t.X[0],t.X[1],otherVx,t.colors[0][0],t.colors[1][0]);
            otherColors[1] = interpolate(t.X[0],t.X[1],otherVx,t.colors[0][1],t.colors[1][1]);
            otherColors[2] = interpolate(t.X[0],t.X[1],otherVx,t.colors[0][2],t.colors[1][2]);
            otherNormal[0] = interpolate(t.X[0],t.X[1],otherVx,t.normals[0][0],t.normals[1][0]);
            otherNormal[1] = interpolate(t.X[0],t.X[1],otherVx,t.normals[0][1],t.normals[1][1]);
            otherNormal[2] = interpolate(t.X[0],t.X[1],otherVx,t.normals[0][2],t.normals[1][2]);
        }
        else {
            otherVx = t.X[1];
            otherZ = interpolate(t.Y[0],t.Y[1],breakPointY,t.Z[0],t.Z[1]);
            otherColors[0] = interpolate(t.Y[0],t.Y[1],breakPointY,t.colors[0][0],t.colors[1][0]);
            otherColors[1] = interpolate(t.Y[0],t.Y[1],breakPointY,t.colors[0][1],t.colors[1][1]);
            otherColors[2] = interpolate(t.Y[0],t.Y[1],breakPointY,t.colors[0][2],t.colors[1][2]);
            otherNormal[0] = interpolate(t.Y[0],t.Y[1],breakPointY,t.normals[0][0],t.normals[1][0]);
            otherNormal[1] = interpolate(t.Y[0],t.Y[1],breakPointY,t.normals[0][1],t.normals[1][1]);
            otherNormal[2] = interpolate(t.Y[0],t.Y[1],breakPointY,t.normals[0][2],t.normals[1][2]);
        }
        
        if (t.Y[0] < t.Y[1]){
            // t.Y[1] for flat bottom, t.Y[0] for flat top
            flatTriangle('b',t.X[2],otherVx,t.X[1],breakPointY,breakPointY,t.Y[1],t.Z[2],otherZ,t.Z[1],t.colors[2],otherColors,t.colors[1],t.normals[2],otherNormal,t.normals[1],buffer, zbuffer, lights, cam);
            flatTriangle('t',t.X[2],otherVx,t.X[0],breakPointY,breakPointY,t.Y[0],t.Z[2],otherZ,t.Z[0],t.colors[2],otherColors,t.colors[0],t.normals[2],otherNormal,t.normals[0],buffer, zbuffer, lights, cam);
        }
        else if (t.Y[1] < t.Y[0]){
            flatTriangle('b',t.X[2],otherVx,t.X[0],breakPointY,breakPointY,t.Y[0],t.Z[2],otherZ,t.Z[0],t.colors[2],otherColors,t.colors[0],t.normals[2],otherNormal,t.normals[0],buffer, zbuffer, lights, cam);
            flatTriangle('t',t.X[2],otherVx,t.X[1],breakPointY,breakPointY,t.Y[1],t.Z[2],otherZ,t.Z[1],t.colors[2],otherColors,t.colors[1],t.normals[2],otherNormal,t.normals[1],buffer, zbuffer, lights, cam);
        }
    }
    else if (((t.Y[1] < t.Y[0]) && (t.Y[0] < t.Y[2])) || ((t.Y[2] < t.Y[0]) && (t.Y[0] < t.Y[1]))){
        breakPointY = t.Y[0];
        if (t.X[1] != t.X[2]){
            slope = (t.Y[2]-t.Y[1])/(t.X[2]-t.X[1]);
            constant = (-t.X[2]*slope)+t.Y[2];
            otherVx = (breakPointY-constant)/slope;
            otherZ = interpolate(t.X[1],t.X[2],otherVx,t.Z[1],t.Z[2]);
            otherColors[0] = interpolate(t.X[1],t.X[2],otherVx,t.colors[1][0],t.colors[2][0]);
            otherColors[1] = interpolate(t.X[1],t.X[2],otherVx,t.colors[1][1],t.colors[2][1]);
            otherColors[2] = interpolate(t.X[1],t.X[2],otherVx,t.colors[1][2],t.colors[2][2]);
            otherNormal[0] = interpolate(t.X[1],t.X[2],otherVx,t.normals[1][0],t.normals[2][0]);
            otherNormal[1] = interpolate(t.X[1],t.X[2],otherVx,t.normals[1][1],t.normals[2][1]);
            otherNormal[2] = interpolate(t.X[1],t.X[2],otherVx,t.normals[1][2],t.normals[2][2]);
        }
        else {
            otherVx = t.X[1];
            otherZ = interpolate(t.Y[1],t.Y[2],breakPointY,t.Z[1],t.Z[2]);
            otherColors[0] = interpolate(t.Y[1],t.Y[2],breakPointY,t.colors[1][0],t.colors[2][0]);
            otherColors[1] = interpolate(t.Y[1],t.Y[2],breakPointY,t.colors[1][1],t.colors[2][1]);
            otherColors[2] = interpolate(t.Y[1],t.Y[2],breakPointY,t.colors[1][2],t.colors[2][2]);
            otherNormal[0] = interpolate(t.Y[1],t.Y[2],breakPointY,t.normals[1][0],t.normals[2][0]);
            otherNormal[1] = interpolate(t.Y[1],t.Y[2],breakPointY,t.normals[1][1],t.normals[2][1]);
            otherNormal[2] = interpolate(t.Y[1],t.Y[2],breakPointY,t.normals[1][2],t.normals[2][2]);
        }
        
        if (t.Y[2] < t.Y[1]){
            // t.Y[1] for flat bottom, t.Y[2] for flat top
            flatTriangle('b',t.X[0],otherVx,t.X[1],breakPointY,breakPointY,t.Y[1],t.Z[0],otherZ,t.Z[1],t.colors[0],otherColors,t.colors[1],t.normals[0],otherNormal,t.normals[1],buffer, zbuffer, lights, cam);
            flatTriangle('t',t.X[0],otherVx,t.X[2],breakPointY,breakPointY,t.Y[2],t.Z[0],otherZ,t.Z[2],t.colors[0],otherColors,t.colors[2],t.normals[0],otherNormal,t.normals[2],buffer, zbuffer, lights, cam);
        }
        else if (t.Y[1] < t.Y[0]){
            flatTriangle('b',t.X[0],otherVx,t.X[2],breakPointY,breakPointY,t.Y[2],t.Z[0],otherZ,t.Z[2],t.colors[0],otherColors,t.colors[2],t.normals[0],otherNormal,t.normals[2],buffer, zbuffer, lights, cam);
            flatTriangle('t',t.X[0],otherVx,t.X[1],breakPointY,breakPointY,t.Y[1],t.Z[0],otherZ,t.Z[1],t.colors[0],otherColors,t.colors[1],t.normals[0],otherNormal,t.normals[1],buffer, zbuffer, lights, cam);
        }
    }
    
    delete[] otherColors;
    delete[] otherNormal;
}

int main()
{
    // Setup of VTK variables and screen
    vtkImageData *image = NewImage(1000, 1000);
    unsigned char *buffer =
    (unsigned char *) image->GetScalarPointer(0,0,0);
    int npixels = 1000*1000;
    for (int i = 0 ; i < npixels*3 ; i++)
        buffer[i] = 0;
    
    std::vector<Triangle> triangles = GetTriangles();
    
    Screen screen;
    screen.buffer = buffer;
    screen.width = 1000;
    screen.height = 1000;
    double **zbuffer = new double*[1000];
    
    // Initialize depth buffer
    for (int i = 0; i < 1000; ++i){
        zbuffer[i] = new double[1000];
        for (int j = 0; j < 1000; ++j){
            zbuffer[i][j] = -1;
        }
    }
    
    LightingParameters *lighting = new LightingParameters();
    
    // Iterate over triangles
    
    // Outer loop deposits triangles one by one
    
    
    // Find out how many triangles we need to iterate over
    double numTriangles = getNum();
    for (int frame = 0; frame < 1000; ++frame){
        if ((frame == 0) || (frame == 250) || (frame == 500) || (frame == 750)){
            
            // Copy original Triangle vector
            std::vector<Triangle> triangles2 = triangles;
            
            // Get camera and matrices
            Camera c = GetCamera(frame,1000);
            Matrix M1 = c.ViewTransform();
            Matrix M2 = c.CameraTransform();
            Matrix M3 = c.DeviceTransform();
            Matrix M4 = Matrix::ComposeMatrices(M2,M1);
            Matrix M5 = Matrix::ComposeMatrices(M4,M3);
            
            // Reset buffers
            for (int i = 0; i < 1000; ++i){
                for (int j = 0; j < 1000; ++j){
                    zbuffer[i][j] = -1;
                }
            }
            for (int i = 0 ; i < npixels*3 ; i++)
                buffer[i] = 0;
            
            for (int t = 0; t < numTriangles; ++t){
                double *oldP1 = new double[4];
                double *oldP2 = new double[4];
                double *oldP3 = new double[4];
                // Assign vertex values
                oldP1[0] = triangles2[t].X[0];
                oldP1[1] = triangles2[t].Y[0];
                oldP1[2] = triangles2[t].Z[0];
                oldP1[3] = 1;
                oldP2[0] = triangles2[t].X[1];
                oldP2[1] = triangles2[t].Y[1];
                oldP2[2] = triangles2[t].Z[1];
                oldP2[3] = 1;
                oldP3[0] = triangles2[t].X[2];
                oldP3[1] = triangles2[t].Y[2];
                oldP3[2] = triangles2[t].Z[2];
                oldP3[3] = 1;
                
                // Find new points
                double *newP1 = new double[4];
                M5.TransformPoint(oldP1, newP1);
                double *newP2 = new double[4];
                M5.TransformPoint(oldP2, newP2);
                double *newP3 = new double[4];
                M5.TransformPoint(oldP3, newP3);
            
                if (newP1[3] != 1){
                    newP1[0] /= newP1[3];
                    newP1[1] /= newP1[3];
                    newP1[2] /= newP1[3];
                    newP1[3] = 1;
                }
                if (newP2[3] != 1){
                    newP2[0] /= newP2[3];
                    newP2[1] /= newP2[3];
                    newP2[2] /= newP2[3];
                    newP2[3] = 1;
                }
                if (newP3[3] != 1){
                    newP3[0] /= newP3[3];
                    newP3[1] /= newP3[3];
                    newP3[2] /= newP3[3];
                    newP3[3] = 1;
                }
                
                // Get new triangle with new points
                triangles2[t].setX(0,newP1[0]);
                triangles2[t].setY(0,newP1[1]);
                triangles2[t].setZ(0,newP1[2]);
                triangles2[t].setX(1,newP2[0]);
                triangles2[t].setY(1,newP2[1]);
                triangles2[t].setZ(1,newP2[2]);
                triangles2[t].setX(2,newP3[0]);
                triangles2[t].setY(2,newP3[1]);
                triangles2[t].setZ(2,newP3[2]);
            
                // Determine if triangle is flat-bottomed
                if (((triangles2[t].Y[0] == triangles2[t].Y[1]) && (triangles2[t].Y[0] < triangles2[t].Y[2])) || ((triangles2[t].Y[2] == triangles2[t].Y[1]) && (triangles2[t].Y[2] < triangles2[t].Y[0])) || ((triangles2[t].Y[2] == triangles2[t].Y[0]) && (triangles2[t].Y[2] < triangles2[t].Y[1]))){
            
                    // Call appropriate routine
                    flatTriangle('b',triangles2[t].X[0],triangles2[t].X[1],triangles2[t].X[2],triangles2[t].Y[0],triangles2[t].Y[1],triangles2[t].Y[2],triangles2[t].Z[0],triangles2[t].Z[1],triangles2[t].Z[2],triangles2[t].colors[0],triangles2[t].colors[1],triangles2[t].colors[2],triangles2[t].normals[0],triangles2[t].normals[1],triangles2[t].normals[2],buffer,zbuffer,lighting,c);
       
                }
        
                // Determine if triangle is flat-topped
                else if (((triangles2[t].Y[0] == triangles2[t].Y[1]) && (triangles2[t].Y[0] > triangles2[t].Y[2])) || ((triangles2[t].Y[2] == triangles2[t].Y[1]) && (triangles2[t].Y[2] > triangles2[t].Y[0])) || ((triangles2[t].Y[2] == triangles2[t].Y[0]) && (triangles2[t].Y[2] > triangles2[t].Y[1]))){
            
            
                    // Call appropriate routine
                    flatTriangle('t',triangles2[t].X[0],triangles2[t].X[1],triangles2[t].X[2],triangles2[t].Y[0],triangles2[t].Y[1],triangles2[t].Y[2],triangles2[t].Z[0],triangles2[t].Z[1],triangles2[t].Z[2],triangles2[t].colors[0],triangles2[t].colors[1],triangles2[t].colors[2],triangles2[t].normals[0],triangles2[t].normals[1],triangles2[t].normals[2],buffer,zbuffer,lighting,c);
            
                }
        
                // Otherwise, split the triangle into flat component triangles
                else {
                    splitTriangle(triangles2[t],buffer,zbuffer,lighting,c);
                }
            
                delete[] oldP1;
                delete[] oldP2;
                delete[] oldP3;
                delete[] newP1;
                delete[] newP2;
                delete[] newP3;
        
            }
            
            std::cout << "frame rendered" << std::endl;
         /*   std::string picture = "c";
            std::stringstream s;
            s << picture << frame;
            std::string imageFile = s.str(); */
            if (frame == 0){
                WriteImage(image, "c1");
            }
            else if (frame == 250){
                WriteImage(image, "c2");
            }
            else if (frame == 500){
                WriteImage(image, "c3");
            }
            else if (frame == 750){
                WriteImage(image, "c4");
            } 
          //  WriteImage(image,imageFile.c_str());
        }
    }
}
