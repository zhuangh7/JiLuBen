package cn.zhuangh7.jiluben.activity.classes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Zhuangh7 on 2016/10/25.
 */
public class dataBase {
    private SQLiteDatabase mDateBase;
    private mDateBase dbHelper;
    private Context context;
    private Boolean isInit = false;
    public dataBase(Context context) {
        this.context = context;
        init();
    }
    private void init(){
        dbHelper = new mDateBase(context,"JILUBEN.db",null,2);
        mDateBase = dbHelper.getWritableDatabase();
        isInit = true;
    }

    public int addShop(String name,String pos,String tel){
        if(isInit){
            if (name.equals("")) {
                return -2;
            }
            mDateBase.execSQL("insert into Shop(name,pos,tel)values(?,?,?)",new Object[]{name,pos,tel});
            Cursor cursor = mDateBase.rawQuery("select ID from Shop where name = ? and pos = ? and tel = ?", new String[]{name, pos, tel});
            while(cursor.moveToNext()){
                int ID = cursor.getInt(0);
                return ID;
            }
        }
        return -1;
    }

    public boolean upNeeds(int ID){
        if(isInit){
            mDateBase.execSQL("update Needs set ifup = 'true' where ID = ?", new Object[]{ID});
            return true;
        }
        return false;
    }
    public boolean deleteNeeds(int ID){
        if(isInit){
            mDateBase.execSQL("delete from Needs where ID = ?",new Object[]{ID});
            return true;
        }
        return false;
    }
    public int addNeeds(int shop_id,String goods_name,int num){
        if (isInit) {
            SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日 HH:mm ");
            Date curDate = new Date(System.currentTimeMillis());
            //获取当前时间
            String str = formatter.format(curDate);
            Log.d("233",str);
            mDateBase.execSQL("insert into Needs(shop_id,good_id,num,ifup,ifcplt) values(?,(select ID from Goods where Goods.name = ?),?,'false',?)",new Object[]{shop_id,goods_name,num,str});
            //TODO add needs
            Cursor cursorID = mDateBase.rawQuery("select ID from Needs where shop_id = ? and good_id = (select ID from Goods where Goods.name = ?) and ifcplt = ? and num = ?", new String[]{""+shop_id, goods_name, str, ""+num});
            while (cursorID.moveToNext()) {
                int ID = cursorID.getInt(0);
                return ID;
            }
        }
        return -1;
    }

    public int addGoods(String name,int defaultNum){
        if(isInit){
            Cursor cursorB = mDateBase.rawQuery("select ID from Goods where name = ?",new String[]{name});
            if(cursorB.getCount()!=0){
                return -1;
            }else{
                mDateBase.execSQL("insert into Goods(name,num)values(?,?)",new Object[]{name,defaultNum});
                Cursor cursor = mDateBase.rawQuery("select ID from Goods where name = ?",new String[]{name});
                cursor.moveToNext();
                int ID = cursor.getInt(0);
                return ID;
            }

        }
        return -1;
    }
    public needsItem[] readGoodsfromNeeds(int shopID){
        needsItem[] result;
        Cursor cursor = mDateBase.rawQuery("select Needs.ID,shop_id,good_id,Needs.num,Goods.name,ifup,ifcplt from Needs,Goods where Goods.ID = Needs.good_id and shop_id = ?", new String[]{"" + shopID});
        int num = cursor.getCount();
        result = new needsItem[num];
        int tag = 0;
        while (cursor.moveToNext()) {
            int ID = cursor.getInt(0);
            int shop_ID = cursor.getInt(1);
            int good_ID = cursor.getInt(2);
            int nnum = cursor.getInt(3);
            String name = cursor.getString(4);
            String ifup = cursor.getString(5);
            String time = cursor.getString(6);
            result[tag++] = new needsItem(ID, shop_ID, good_ID, nnum,name, ifup, time);
        }
        return result;
    }
    public goodsItem[] readGoods(){
        goodsItem[] result;
        Cursor cursor = mDateBase.query("Goods", new String[]{"ID", "name", "num"}, null, null, null, null, null, null);
        int num = cursor.getCount();
        result = new goodsItem[num];
        int tag = 0;
        while(cursor.moveToNext()){
            int ID = cursor.getInt(0);
            String name = cursor.getString(1);
            int numm = cursor.getInt(2);
            result[tag++] = new goodsItem(name, numm, ID);
        }
        return result;
    }
    public boolean ifShopHasNeeds(int id){
        Cursor cursor = mDateBase.rawQuery("select * from Needs where shop_id = ?",new String[]{""+id});
        if(cursor.getCount()!=0){
            return true;
        }
        else{
            return false;
        }
    }
    public mainItem getShopbyID(int ID) {
        mainItem result;
        Cursor cursor = mDateBase.rawQuery("select name,pos,tel from Shop where ID = ?", new String[]{""+ID});
        int num = cursor.getCount();
        //result = new mainItem();
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String pos = cursor.getString(1);
            String tel = cursor.getString(2);

            result = new mainItem(name, pos, tel, ID,ifShopHasNeeds(ID));
            return result;
        }
        return null;
    }
    public mainItem[] readShop(){
        mainItem[] result;
        Cursor cursor = mDateBase.query("Shop", new String[]{"ID","name","pos","tel"}, null, null, null, null, null, null);
        int num = cursor.getCount();
        result = new mainItem[num];
        int tag = 0;
        while(cursor.moveToNext()){
            int ID = cursor.getInt(0);
            String name = cursor.getString(1);
            String pos = cursor.getString(2);
            String tel = cursor.getString(3);
            result[tag++] = new mainItem(name,pos,tel,ID,ifShopHasNeeds(ID));
        }
        return result;
    }

    public void deleteData(){
        mDateBase.execSQL("drop table if exists Shop");
        mDateBase.execSQL("drop table if exists Goods");
        mDateBase.execSQL("drop table if exists Needs");
    }
    //TODO DON'T USE

    public boolean deleteShop(String name,String pos,String tel){
        if(isInit){
            mDateBase.execSQL("delete from Shop where Shop.name = ? and Shop.pos = ? and Shop.tel = ?",new Object[]{name,pos,tel});
            return true;
        }
        return false;
    }

    public boolean deleteShop(int ID){
        if(isInit){
            mDateBase.execSQL("delete from Shop where Shop.ID = ?",new Object[]{ID});
            mDateBase.execSQL("delete from Needs where shop_id = ?",new Object[]{ID});
            return true;
        }
        return false;
    }

    public boolean deleteGoods(int ID){
        if (isInit) {
            mDateBase.execSQL("delete from Goods where Goods.ID = ?",new Object[]{ID});
            mDateBase.execSQL("delete from Needs where good_id = ?", new Object[]{ID});
            return true;
        }
        return false;
    }
    public boolean updateShop(int ID,String name,String pos,String tel){
        if(isInit){
            String sql = "update Shop set";
            if(ID == -1){
                return false;
            }else{
                if(name!=null){
                    sql+=" name = "+name+",";
                }
                if(pos!=null){
                    sql+=" pos = "+pos+",";
                }
                if(tel!=null){
                    sql+=" tel = "+tel;
                }
                if(name==null && pos ==null && tel ==null){
                    return false;
                }
                else{
                    sql+=" where ID = "+ID;
                    mDateBase.execSQL(sql);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateGoods(int ID,String name,int num){
        if(isInit){
            if(name.equals(""))
                return false;
            String sql = "update Goods set";
            if(name!=null){
                Cursor cursorB = mDateBase.rawQuery("select ID from Goods where name = ?",new String[]{name});
                if(cursorB.getCount()!=0){
                    return false;
                }
            }

            if(ID == -1){
                return false;
            }else{
                if(name != null){
                    sql+=" name = '"+name+"'";
                }
                if(num!=-2){
                    sql+=" num = "+num;
                }
                sql+=" where ID = "+ID;
                mDateBase.execSQL(sql);
                return true;
            }

        }
        return false;
    }

}