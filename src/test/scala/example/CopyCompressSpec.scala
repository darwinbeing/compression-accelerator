package example

import org.scalatest.{FlatSpec, Matchers}
import scala.math._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class CopyCompressSpec extends FlatSpec with Matchers{
    behavior of "CopyCompress"

    val params = new CopyCompressParams{
        val parallellane = 1
    }

    // Because the maximum length is 64, then the test case vector will be 70
    val testNum = 3
    val vecLength = 70
    val candidateVec = Array.ofDim[List[Int]](testNum)
    val dataVec = Array.ofDim[List[Int]](testNum)
    val rangen = new Random(15)
    val maxOneByte = pow(2,8).toInt
    val maxTwoByte = pow(2,16).toInt

    val maxOneByteOffset = pow(2,11).toInt

    // test case 1:
    // - no buffer invalid problem
    // - parallel lane number is 1
    // - number of copy data is (copyByte) bytes
    // - test offset with 1 byte

    val copyByteTest1 = 4 + rangen.nextInt(8) 
    val sameDataTest1 = Array.fill[Int](copyByteTest1)(rangen.nextInt(maxOneByte)) // fill array with 1 byte integers
    val dummyCandidateTest1 = Array.fill[Int](vecLength-copyByteTest1)(1)
    val dummyDataTest1 = Array.fill[Int](vecLength-copyByteTest1)(2)

    candidateVec(0) = sameDataTest1.toList ++ dummyCandidateTest1.toList
    dataVec(0) = sameDataTest1.toList ++ dummyDataTest1.toList


    // test case 2:
    // - no buffer invalid problem
    // - parallel lane number is 1
    // - number of copy data is 32 bytes
    // - test offset with 2 bytes and 4 bytes

    val copyByteTest2 = 1 + rangen.nextInt(63)

    val sameDataTest2 = Array.fill[Int](copyByteTest2)(rangen.nextInt(maxOneByte)) // fill array with 1 byte integers
    val dummyCandidateTest2 = Array.fill[Int](vecLength-copyByteTest2)(1)
    val dummyDataTest2 = Array.fill[Int](vecLength-copyByteTest2)(2)

    candidateVec(1) = sameDataTest2.toList ++ dummyCandidateTest2.toList
    candidateVec(2) = sameDataTest2.toList ++ dummyCandidateTest2.toList

    dataVec(1) = sameDataTest2.toList ++ dummyDataTest1.toList 
    dataVec(2) = sameDataTest2.toList ++ dummyDataTest2.toList
    
    // generate 1/2/4 bytes offset between data and candidate
    val offsetVec = List(BigInt(rangen.nextInt(maxOneByteOffset)), BigInt(rangen.nextInt(maxTwoByte)), BigInt(rangen.nextInt)/*0x4db39d07*/)

    println("copyBytes for each tests are:")
    print(copyByteTest1)
    print("\n")
    print(copyByteTest2)
    print("\n")
    print(copyByteTest2)
    print("\n")





    println("offset for each test is:")
    offsetVec.foreach((a) => println(a.toString(16)))

    // get golden result
    // Test1 result
    val testResult1: BigInt = (offsetVec(0) & 0x0FF) | ((offsetVec(0) & 0x700) << 5) |(BigInt(copyByteTest1 - 4) << 10) | (BigInt(1) << 8)
    val testResult2: BigInt = ((offsetVec(1) & 0xFF00) >> 8) | ((offsetVec(1) & 0x00FF) << 8) | (BigInt(copyByteTest2-1) << 18) | (BigInt(2) << 16)
    val testResult3: BigInt = ((offsetVec(2) & 0xFF000000) >> 24) | ((offsetVec(2) & 0x00FF0000) >> 8) | ((offsetVec(2) & 0x0000FF00) << 8) | ((offsetVec(2) & 0x000000FF) << 24) | (BigInt(3) << 32) | (BigInt(copyByteTest2-1) << 34) & (0x00000FFFFFFFFFFL)

    val goldenRes = List(testResult1, testResult2, testResult3)
    println("goldenRes are:")
    goldenRes.foreach((a) => println(a.toString(16)))

    it should "Test copy compression case 2" in{
       DoCopyCompressTesterLane1(params, candidateVec, dataVec, offsetVec, goldenRes) should be (true)
    }  
    
    val params_2 = new CopyCompressParams{
        val parallellane = 2
    }
    print(params_2.parallellane)

    it should "Test copy compression case 3" in{
       DoCopyCompressTesterLane2(params_2, candidateVec, dataVec, offsetVec, goldenRes) should be (true)
    }
    


}
