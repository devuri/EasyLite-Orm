package com.easyliteorm;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.easyliteorm.model.Book;

@RunWith(RobolectricTestRunner.class)
public class EntityScannerTest {
	private Context context;
	@Before public void setup (){
		this.context = Robolectric.buildActivity(Activity.class).create().get();
	}
	
	@Test public void getAllClassesTest () throws NameNotFoundException, IOException{
		List<String> result = EntityScanner.getAllClasses(context);
		Assert.assertNotNull(result);
		Assert.assertFalse(result.isEmpty());
		Assert.assertTrue(result.contains(Book.class.getName()));
	}
	
	
	@After public void tearDown (){
		this.context = null;
	}
}
