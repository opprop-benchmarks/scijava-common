/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests {@link Types}.
 * 
 * @author Curtis Rueden
 * @author Mark Hiner
 */
public class TypesTest {

	/** Tests {@link Types#load}. */
	@Test
	public void testLoad() {
		assertLoaded(boolean.class, "boolean");
		assertLoaded(byte.class, "byte");
		assertLoaded(char.class, "char");
		assertLoaded(double.class, "double");
		assertLoaded(float.class, "float");
		assertLoaded(int.class, "int");
		assertLoaded(long.class, "long");
		assertLoaded(short.class, "short");
		assertLoaded(void.class, "void");
		assertLoaded(String.class, "string");
		assertLoaded(Number.class, "java.lang.Number");
		assertLoaded(boolean[].class, "boolean[]");
		assertLoaded(byte[].class, "byte[]");
		assertLoaded(char[].class, "char[]");
		assertLoaded(double[].class, "double[]");
		assertLoaded(float[].class, "float[]");
		assertLoaded(int[].class, "int[]");
		assertLoaded(long[].class, "long[]");
		assertLoaded(short[].class, "short[]");
		assertLoaded(null, "void[]");
		assertLoaded(String[].class, "string[]");
		assertLoaded(Number[].class, "java.lang.Number[]");
		assertLoaded(boolean[][].class, "boolean[][]");
		assertLoaded(byte[][].class, "byte[][]");
		assertLoaded(char[][].class, "char[][]");
		assertLoaded(double[][].class, "double[][]");
		assertLoaded(float[][].class, "float[][]");
		assertLoaded(int[][].class, "int[][]");
		assertLoaded(long[][].class, "long[][]");
		assertLoaded(short[][].class, "short[][]");
		assertLoaded(null, "void[][]");
		assertLoaded(String[][].class, "string[][]");
		assertLoaded(Number[][].class, "java.lang.Number[][]");
		assertLoaded(boolean[].class, "[Z");
		assertLoaded(byte[].class, "[B");
		assertLoaded(char[].class, "[C");
		assertLoaded(double[].class, "[D");
		assertLoaded(float[].class, "[F");
		assertLoaded(int[].class, "[I");
		assertLoaded(long[].class, "[J");
		assertLoaded(short[].class, "[S");
		assertLoaded(null, "[V");
		assertLoaded(String[].class, "[Lstring;");
		assertLoaded(Number[].class, "[Ljava.lang.Number;");
		assertLoaded(boolean[][].class, "[[Z");
		assertLoaded(byte[][].class, "[[B");
		assertLoaded(char[][].class, "[[C");
		assertLoaded(double[][].class, "[[D");
		assertLoaded(float[][].class, "[[F");
		assertLoaded(int[][].class, "[[I");
		assertLoaded(long[][].class, "[[J");
		assertLoaded(short[][].class, "[[S");
		assertLoaded(null, "[[V");
		assertLoaded(String[][].class, "[[Lstring;");
		assertLoaded(Number[][].class, "[[Ljava.lang.Number;");
	}

	/** Tests {@link Types#load}. */
	@Test
	public void testLoadFailureQuiet() {
		// test quiet failure
		assertNull(Types.load("a.non.existent.class"));
	}

	/** Tests {@link Types#load}. */
	@Test(expected = IllegalArgumentException.class)
	public void testLoadFailureLoud() {
		Types.load("a.non.existent.class", false);
	}

	/** Tests {@link Types#name}. */
	public void testName() {
		@SuppressWarnings("unused")
		class Struct {

			private List<String> list;
		}
		assertEquals("boolean", Types.name(boolean.class));
		assertEquals("java.lang.String", Types.name(String.class));
		assertEquals("List<String>[]", Types.name(type(Struct.class, "list")));
	}

	/** Tests {@link Types#raw(Type)}. */
	@Test
	public void testRaw() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String> list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int[].class, raw(Struct.class, "intArray"));
		assertSame(double.class, raw(Struct.class, "d"));
		assertSame(String[][].class, raw(Struct.class, "strings"));
		assertSame(Void.class, raw(Struct.class, "v"));
		assertSame(List.class, raw(Struct.class, "list"));
		assertSame(HashMap.class, raw(Struct.class, "map"));
	}

	/** Tests {@link Types#raws}. */
	@Test
	public void testRaws() {
		final Field field = Types.field(Thing.class, "thing");

		// Object
		assertAllTheSame(Types.raws(Types.type(field, Thing.class)), Object.class);

		// N extends Number
		assertAllTheSame(Types.raws(Types.type(field, NumberThing.class)),
			Number.class);

		// Integer
		assertAllTheSame(Types.raws(Types.type(field, IntegerThing.class)),
			Integer.class);

		// Serializable & Cloneable
		assertAllTheSame(Types.raws(Types.type(field, ComplexThing.class)),
			Serializable.class, Cloneable.class);
	}

	/** Tests {@link Types#box(Class)}. */
	@Test
	public void testBox() {
		final Class<Boolean> booleanType = Types.box(boolean.class);
		assertSame(Boolean.class, booleanType);

		final Class<Byte> byteType = Types.box(byte.class);
		assertSame(Byte.class, byteType);

		final Class<Character> charType = Types.box(char.class);
		assertSame(Character.class, charType);

		final Class<Double> doubleType = Types.box(double.class);
		assertSame(Double.class, doubleType);

		final Class<Float> floatType = Types.box(float.class);
		assertSame(Float.class, floatType);

		final Class<Integer> intType = Types.box(int.class);
		assertSame(Integer.class, intType);

		final Class<Long> longType = Types.box(long.class);
		assertSame(Long.class, longType);

		final Class<Short> shortType = Types.box(short.class);
		assertSame(Short.class, shortType);

		final Class<Void> voidType = Types.box(void.class);
		assertSame(Void.class, voidType);

		final Class<?>[] types = { //
			Boolean.class, Byte.class, Character.class, Double.class, //
				Float.class, Integer.class, Long.class, Short.class, //
				Void.class, //
				String.class, //
				Number.class, BigInteger.class, BigDecimal.class, //
				boolean[].class, byte[].class, char[].class, double[].class, //
				float[].class, int[].class, long[].class, short[].class, //
				Boolean[].class, Byte[].class, Character[].class, Double[].class, //
				Float[].class, Integer[].class, Long[].class, Short[].class, //
				Void[].class, //
				Object.class, Object[].class, String[].class, //
				Object[][].class, String[][].class, //
				Collection.class, //
				List.class, ArrayList.class, LinkedList.class, //
				Set.class, HashSet.class, //
				Map.class, HashMap.class, //
				Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Class<?> type = Types.box(c);
			assertSame(c, type);
		}
	}

	/** Tests {@link Types#nullValue(Class)}. */
	@Test
	public void testNullValue() {
		final boolean booleanNull = Types.nullValue(boolean.class);
		assertFalse(booleanNull);

		final byte byteNull = Types.nullValue(byte.class);
		assertEquals(0, byteNull);

		final char charNull = Types.nullValue(char.class);
		assertEquals('\0', charNull);

		final double doubleNull = Types.nullValue(double.class);
		assertEquals(0.0, doubleNull, 0.0);

		final float floatNull = Types.nullValue(float.class);
		assertEquals(0f, floatNull, 0f);

		final int intNull = Types.nullValue(int.class);
		assertEquals(0, intNull);

		final long longNull = Types.nullValue(long.class);
		assertEquals(0, longNull);

		final short shortNull = Types.nullValue(short.class);
		assertEquals(0, shortNull);

		final Void voidNull = Types.nullValue(void.class);
		assertNull(voidNull);

		final Class<?>[] types = { //
			Boolean.class, Byte.class, Character.class, Double.class, //
			Float.class, Integer.class, Long.class, Short.class, //
			Void.class, //
			String.class, //
			Number.class, BigInteger.class, BigDecimal.class, //
			boolean[].class, byte[].class, char[].class, double[].class, //
			float[].class, int[].class, long[].class, short[].class, //
			Boolean[].class, Byte[].class, Character[].class, Double[].class, //
			Float[].class, Integer[].class, Long[].class, Short[].class, //
			Void[].class, //
			Object.class, Object[].class, String[].class, //
			Object[][].class, String[][].class, //
			Collection.class, //
			List.class, ArrayList.class, LinkedList.class, //
			Set.class, HashSet.class, //
			Map.class, HashMap.class, //
			Collection[].class, List[].class, Set[].class, Map[].class };
		for (final Class<?> c : types) {
			final Object nullValue = Types.nullValue(c);
			assertNull("Expected null for " + c.getName(), nullValue);
		}
	}

	/** Tests {@link Types#field}. */
	@Test
	public void testField() {
		final Field field = Types.field(Thing.class, "thing");
		assertEquals("thing", field.getName());
		assertSame(Object.class, field.getType());
		assertTrue(field.getGenericType() instanceof TypeVariable);
		assertEquals("T", ((TypeVariable<?>) field.getGenericType()).getName());
	}

	/** Tests {@link Types#array}. */
	@Test
	public void testArray() {
		// 1-dimensional cases
		assertSame(boolean[].class, Types.array(boolean.class));
		assertSame(String[].class, Types.array(String.class));
		assertSame(Number[].class, Types.array(Number.class));
		assertSame(boolean[][].class, Types.array(boolean[].class));
		assertSame(String[][].class, Types.array(String[].class));
		assertSame(Number[][].class, Types.array(Number[].class));
		try {
			Types.array(void.class);
			fail("Unexpected success creating void[]");
		}
		catch (final IllegalArgumentException exc) { }

		// multidimensional cases
		assertSame(Number[][].class, Types.array(Number.class, 2));
		assertSame(boolean[][][].class, Types.array(boolean.class, 3));
		assertSame(String.class, Types.array(String.class, 0));
		try {
			Types.array(char.class, -1);
			fail("Unexpected success creating negative dimensional array");
		}
		catch (final IllegalArgumentException exc) { }
	}

	/** Tests {@link Types#component(Type)}. */
	@Test
	public void testComponent() {
		@SuppressWarnings("unused")
		class Struct {

			private int[] intArray;
			private double d;
			private String[][] strings;
			private Void v;
			private List<String>[] list;
			private HashMap<Integer, Float> map;
		}
		assertSame(int.class, componentType(Struct.class, "intArray"));
		assertNull(componentType(Struct.class, "d"));
		assertSame(String[].class, componentType(Struct.class, "strings"));
		assertSame(null, componentType(Struct.class, "v"));
		assertSame(List.class, componentType(Struct.class, "list"));
		assertSame(null, componentType(Struct.class, "map"));
	}

	/** Tests {@link Types#type(Field, Class)}. */
	@Test
	public void testTypeField() {
		final Field field = Types.field(Thing.class, "thing");

		// T
		final Type tType = Types.type(field, Thing.class);
		assertEquals("capture of ?", tType.toString());

		// N extends Number
		final Type nType = Types.type(field, NumberThing.class);
		assertEquals("capture of ?", nType.toString());

		// Integer
		final Type iType = Types.type(field, IntegerThing.class);
		assertSame(Integer.class, iType);
	}

	/** Tests {@link Types#param}. */
	@Test
	public void testParam() {
		class Struct {

			@SuppressWarnings("unused")
			private List<int[]> list;
		}
		final Type listType = type(Struct.class, "list");
		final Type paramType = Types.param(listType, List.class, 0);
		final Class<?> paramClass = Types.raw(paramType);
		assertSame(int[].class, paramClass);
	}

	// -- Helper classes --

	private static class Thing<T> {
		@SuppressWarnings("unused")
		private T thing;
	}

	private static class NumberThing<N extends Number> extends Thing<N> {
		// NB: No implementation needed.
	}

	private static class IntegerThing extends NumberThing<Integer> {
		// NB: No implementation needed.
	}

	private static class ComplexThing<T extends Serializable & Cloneable> extends
		Thing<T>
	{
		// NB: No implementation needed.
	}

	// -- Helper methods --

	/** Convenience method to get the {@link Type} of a field. */
	private Type type(final Class<?> c, final String fieldName) {
		return Types.field(c, fieldName).getGenericType();
	}

	/** Convenience method to call {@link Types#raw} on a field. */
	private Class<?> raw(final Class<?> c, final String fieldName) {
		return Types.raw(type(c, fieldName));
	}

	/** Convenience method to call {@link Types#component} on a field. */
	private Class<?> componentType(final Class<?> c, final String fieldName) {
		return Types.raw(Types.component(type(c, fieldName)));
	}

	private void assertLoaded(final Class<?> c, final String name) {
		assertSame(c, Types.load(name));
	}

	private void assertAllTheSame(final List<?> list, final Object... values) {
		assertEquals(list.size(), values.length);
		for (int i = 0; i < values.length; i++) {
			assertSame(list.get(i), values[i]);
		}
	}

}
