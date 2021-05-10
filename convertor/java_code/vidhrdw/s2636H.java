extern int s2636_x_offset;
extern int s2636_y_offset;

void s2636_w(UBytePtr workram,int offset,int data,UBytePtr dirty);
void Update_Bitmap(struct osd_bitmap *bitmap,UBytePtr workram,UBytePtr dirty,int Graphics_Bank,struct osd_bitmap *collision_bitmap);

