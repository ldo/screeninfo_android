/*
    Common code for writing/displaying members of an info structure.

    Copyright © 2012 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

package com.jotabout.screeninfo;

public abstract class InfoActivity extends android.app.Activity
  {

    public abstract class InfoMember
      /* obtaining and displaying a value from a member of an info structure.
        Needs Activity context so it can call findViewById. */
      {
        final Object InObject;
        /*final*/ java.lang.reflect.Method ToString;
        final int TextID; /* View ID or string ID, depending */

        public InfoMember
          (
            Object InObject, /* the info structure */
            int TextID
          )
          {
            this.InObject = InObject;
            this.TextID = TextID;
          } /*InfoMember*/

        protected void Init
          (
            Class<?> MemberType
          )
          /* remainder of common initialization that can't be done in constructor. */
          {
            if (MemberType.isPrimitive())
              {
                try
                  {
                    final String TypeName = MemberType.getName().intern();
                    if (TypeName == "int")
                      {
                        MemberType = Class.forName("java.lang.Integer");
                      }
                    else if (TypeName == "float")
                      {
                        MemberType = Class.forName("java.lang.Float");
                      }
                    else if (TypeName == "double")
                      {
                        MemberType = Class.forName("java.lang.Double");
                      } /*if*/
                  /* add other replacements of primitive types here as necessary */
                  }
                catch (ClassNotFoundException err)
                  {
                    throw new RuntimeException(err.toString());
                  } /*try*/
              } /*if*/
            try
              {
                this.ToString = MemberType.getDeclaredMethod("toString");
              }
            catch (NoSuchMethodException err)
              {
                throw new RuntimeException(err.toString());
              } /*try*/
          } /*Init*/

        abstract public Object GetValue();
          /* must return the value to be displayed. */

        public String GetStringValue()
          /* returns the value as a String. */
          {
            try
              {
                return
                    (String)ToString.invoke(GetValue());
              }
            catch (IllegalAccessException err)
              {
                throw new RuntimeException(err.toString());
              }
            catch (java.lang.reflect.InvocationTargetException err)
              {
                throw new RuntimeException(err.toString());
              } /*try*/
          } /*GetStringValue*/

        public void ShowValue()
          /* sets the TextView with ID TextID to show the member return value. */
          {
            ((android.widget.TextView)findViewById(TextID)).setText(GetStringValue());
          } /*ShowValue*/

        public void AppendValue
          (
            StringBuilder ToString
          )
          /* appends the member return value to the string being built, prefixed
            by the string with ID TextID. */
          {
            ToString
                .append(getString(TextID))
                .append(" ")
                .append(GetStringValue())
                .append("\n");
          } /*AppendValue*/

      } /*InfoMember*/;

    public class InfoField extends InfoMember
      /* obtaining and displaying a field value from an info structure. */
      {
        final java.lang.reflect.Field ObjField;

        public InfoField
          (
            Object InObject, /* the info structure */
            String FieldName, /* value of this field will be shown */
            int TextID /* ID of TextView to be set to value */
          )
          {
            super(InObject, TextID);
            try
              {
                this.ObjField = InObject.getClass().getDeclaredField(FieldName);
              }
            catch (NoSuchFieldException err)
              {
                throw new RuntimeException(err.toString());
              } /*try*/
            Init(this.ObjField.getType());
          } /*InfoField*/

        public Object GetValue()
          {
            try
              {
                return
                    ObjField.get(InObject);
              }
            catch (IllegalAccessException err)
              {
                throw new RuntimeException(err.toString());
              } /*try*/
          } /*GetValue*/

      } /*InfoField*/;

    public class InfoMethod extends InfoMember
      /* obtaining and displaying a method return value from an info structure. */
      {
        final java.lang.reflect.Method ObjMethod;

        public InfoMethod
          (
            Object InObject, /* the info structure */
            String MethodName, /* must take no arguments */
            int TextID /* ID of TextView to be set to value */
          )
          {
            super(InObject, TextID);
            try
              {
                this.ObjMethod = InObject.getClass().getDeclaredMethod(MethodName);
              }
            catch (NoSuchMethodException err)
              {
                throw new RuntimeException(err.toString());
              } /*try*/
            Init(this.ObjMethod.getReturnType());
          } /*InfoMethod*/

        public Object GetValue()
          {
            try
              {
                return
                    ObjMethod.invoke(InObject);
              }
            catch (IllegalAccessException err)
              {
                throw new RuntimeException(err.toString());
              }
            catch (java.lang.reflect.InvocationTargetException err)
              {
                throw new RuntimeException(err.toString());
              } /*try*/
          } /*GetValue*/

      } /*InfoMethod*/;

  } /*InfoActivity*/;
